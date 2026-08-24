# realworld-spring

A Spring Boot implementation of the [RealWorld](https://realworld-docs.netlify.app/specifications/backend/endpoints/)
("Conduit") backend API.

[![API tests](https://github.com/akydd/realworld-spring/actions/workflows/api-tests.yml/badge.svg)](https://github.com/akydd/realworld-spring/actions/workflows/api-tests.yml)

## Tech stack

| Concern        | Choice                                    |
|----------------|-------------------------------------------|
| Language       | Java 26                                   |
| Framework      | Spring Boot 4.1 (Spring Framework 7)      |
| Web            | Spring MVC (`spring-boot-starter-webmvc`) |
| Persistence    | Spring Data JPA + PostgreSQL              |
| Migrations     | Flyway                                    |
| Security       | Spring Security + JWT (jjwt)              |
| Mapping        | MapStruct                                 |
| JSON           | Jackson 3 (`tools.jackson`, via Boot 4)   |
| Build          | Gradle                                    |

## Running locally

Prerequisites: a JDK for **Java 26** and **Docker** (used for the database).

### Run the app

`spring-boot-docker-compose` is on the dev classpath, so `bootRun` automatically starts the
PostgreSQL container from `compose.yaml` (Postgres 18 on `localhost:8095`) and wires the datasource
to it — no manual database setup. Flyway then applies the migrations in
`src/main/resources/db/migration` on startup.

```bash
./gradlew bootRun
```

- API: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health` (with `/actuator/health/readiness` and `/liveness`)

Docker must be running. To run against your own PostgreSQL instead (no Docker), disable the module
with `spring.docker.compose.enabled=false`; the app then uses the datasource defaults in
`application.properties` (`localhost:8095`, `admin`/`password`), overridable via the
`SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` environment variables.

### Unit tests

```bash
./gradlew test
```

### API tests (JUnit ports of the Hurl specs)

`src/test/java/com/akydd/realworld_spring/api` holds JUnit reimplementations of the RealWorld Hurl
specs — the same requests and response assertions, but as Java integration tests. Each `*ApiTest`
class mirrors one `.hurl` file (e.g. `ArticlesApiTest` ↔ `articles.hurl`, `FeedApiTest` ↔
`feed.hurl`). They boot the full app on a random port (`@SpringBootTest(webEnvironment =
RANDOM_PORT)`) and drive it over HTTP with `TestRestTemplate`, sending and reading **raw JSON** so
the app's wrap/unwrap-root-value settings are exercised end to end.

The database is a **Testcontainers** PostgreSQL 18 container, started once and shared across all
suites (`ApiTestSupport`) and wired in with `@ServiceConnection` — so, unlike the Hurl flow below,
there is no app or database to start by hand. **Docker must be running.**

```bash
# every API test
./gradlew test --tests 'com.akydd.realworld_spring.api.*'

# a single suite
./gradlew test --tests 'com.akydd.realworld_spring.api.ArticlesApiTest'
```

(`./gradlew test` runs these too, alongside the unit tests.) They also run in CI on every push to
`main` — see `.github/workflows/api-tests.yml`.

### End-to-end (Hurl) tests

The RealWorld spec's own HTTP tests (Hurl) live in the upstream repo under `specs/api/hurl` and run
against the **running** app over the network — unlike the API tests above, which boot the app
in-process. The `make e2e` target wraps the whole dance:

```bash
make e2e
```

Under the hood (`scripts/e2e.sh`) it:

1. builds the jar (`./gradlew bootJar`) first, so a broken build fails fast before touching Docker;
2. starts a **disposable** test database (`compose.test.yaml`, Postgres 18 on `localhost:8096`, no
   volume — so migrations always apply fresh) and waits until it is healthy;
3. runs the jar against it — the jar excludes the `developmentOnly` docker-compose module, so it
   honours the `SPRING_DATASOURCE_*` env vars instead of auto-starting `compose.yaml`;
4. polls `/actuator/health/readiness` until the app reports `UP`;
5. runs the Hurl suite (`run-hurl-tests.sh`) against it;
6. **always tears down on exit** (even on failure or Ctrl-C) — kills the app and
   `docker compose down -v`.

Prerequisites: Docker running, and [Hurl](https://hurl.dev) installed (used by `run-hurl-tests.sh`).

`HOST` and `HURL_DIR` are overridable; the defaults are shown below. `HURL_DIR` must point at your
local checkout of the RealWorld spec repo:

```bash
make e2e HOST=http://localhost:8080 HURL_DIR=../realworld/specs/api/hurl
```

Two helper targets manage the throwaway DB on its own — `make test-db-up` and `make test-db-down` —
handy when iterating on Hurl files against an app you are already running.

### Maintenance commands (Spring Shell)

One-off maintenance tasks are exposed as [Spring Shell](https://spring.io/projects/spring-shell)
commands. The interactive shell is disabled (`spring.shell.interactive.enabled=false`), so a normal
launch is unaffected — a command runs only when passed as an argument, then the process exits.

`reconcile-counts` rebuilds every article's cached `favoritesCount` from the `article_favorites`
source of truth (see [Favorites count](#favorites-count-a-denormalized-counter-cache)):

```bash
SPRING_MAIN_WEB_APPLICATION_TYPE=none \
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:<port>/<db> \
SPRING_DATASOURCE_USERNAME=<user> SPRING_DATASOURCE_PASSWORD=<pass> \
java -jar build/libs/*-SNAPSHOT.jar reconcile-counts
```

It prints e.g. `Reconciled favoritesCount for 12 article(s).` and exits.

**Pass configuration as environment variables, not `--flags`.** Spring Shell treats the whole
program-argument list as the command line, so `--spring.*` flags would be parsed as (unexpected)
arguments to `reconcile-counts` and fail. `SPRING_MAIN_WEB_APPLICATION_TYPE=none` keeps it a plain
process — no web server — that runs the command and exits.

## Authentication notes

- The API expects the RealWorld auth scheme: `Authorization: Token <jwt>` (not `Bearer`).
- The JWT **subject is the immutable user id**, deliberately *not* the username or email — the spec
  allows a user to change both via `PUT /api/user`, and putting a mutable value in the token would
  invalidate outstanding tokens (or, worse, let a recycled name resolve to a different user) after a
  rename. Login authenticates by email; the token then carries the id; the auth filter resolves the
  caller by id.

## Favorites count: a denormalized counter cache

`Article.favoritesCount` is a **denormalized counter cache**: the favorite count is stored on the
article row rather than derived with `count(*)` on every read. This mirrors Rails' `counter_cache`
and exists for the same reason — the feed and list endpoints return many articles at once, so
deriving the count would mean a `count` per article; a plain column read is far cheaper at volume.

The counter is maintained incrementally: `favorite`/`unfavorite` adjust it with an atomic
`favoritesCount = favoritesCount ± 1` in the **same transaction** as the `article_favorites` link
change, so the two stay consistent within the app (and a duplicate-favorite race loses on the
`article_favorites` composite key, rolling its increment back).

**The trade-off** is that a cached count can still drift from the source of truth — a bulk import,
direct SQL, or a bug that touches `article_favorites` without going through the service. So, as Rails
ships `reset_counters`, there is a reconciliation that rebuilds every counter from the join table in
one statement (`ArticleRepository.reconcileFavoritesCounts`), exposed as the `reconcile-counts`
[maintenance command](#maintenance-commands-spring-shell). It is safe to run against a live app
(it's a pure DB operation), ideally during a quiet period — a favorite committing mid-run could
leave that one article a hair stale until the next run.

> At this project's data volume the counter is arguably premature; deriving the count on read would
> be simpler and drift-proof. It is kept deliberately, to exercise the counter-cache pattern.

## Follows: a join entity, not a `@ManyToMany`

The follow relationship is modelled as a dedicated **join entity** — `Follows`, with a composite
`@EmbeddedId` over `(follower_id, following_id)` and two `@MapsId` associations — rather than a JPA
`@ManyToMany Set<User>` collection on `User`. This is a deliberate choice for **lookup cost at
scale**.

With a `@ManyToMany` collection, following someone reads as:

```java
if (!me.getFollowing().contains(target)) { me.addFollowing(target); }
```

but `getFollowing().contains(...)` forces Hibernate to **fully initialize the collection** — it loads
*every* user you already follow into memory just to answer one membership question and insert one
row. For someone who follows hundreds of thousands of accounts, a single follow/unfollow becomes
O(N) in rows and memory. It doesn't scale.

Through the join entity, every operation is an O(1) primary-key access and no collection is ever
hydrated:

- **follow** → `existsById(id)` (PK index lookup), then `save(new Follows(me, target))`
- **unfollow** → `existsById(id)`, then `deleteById(id)`
- **"is X following Y?"**, and the feed/list `following` flags → indexed `exists`/`count` queries
  against `Follows` (`UserRepository.isFollowing` and the `ArticleRepository` list/feed subqueries)

The composite primary key `(follower_id, following_id)` is the source of truth and makes the writes
naturally idempotent — a duplicate follow can't create a second row. `User` holds no follows
collection at all; `Follows` is the single representation, used for both reads and writes. (The same
reasoning applies to article favorites via `article_favorites`.)

## Development issues encountered

Notes on non-obvious problems hit while building this, kept for future reference.

### `JsonNullable` doesn't work with Jackson 3 (Spring Boot 4)

**Symptom.** `PUT /api/user` failed with:

```
Type definition error: [simple type, class org.openapitools.jackson.nullable.JsonNullable]
```

even though the `JsonNullableModule` bean was correctly registered.

**Why partial-update needs a special type.** The RealWorld update endpoint has *tri-state* fields
(`bio`, `image`). The spec tests require three distinct behaviours that a plain nullable `String`
cannot express, because it collapses "absent" and "null" into the same value:

| Request body        | Expected result           |
|---------------------|---------------------------|
| field omitted       | keep the existing value   |
| `"field": "value"`  | set to the value          |
| `"field": null`     | clear the field to null   |
| `"field": ""`       | clear the field to null   |

The usual solution is `org.openapitools:jackson-databind-nullable`'s `JsonNullable<T>`, which
distinguishes *undefined* from *present-and-null*.

**Root cause.** Spring Boot 4.1 switched its default JSON mapper to **Jackson 3** (the new
`tools.jackson` namespace). The classpath ends up with *both* Jacksons:

```
com.fasterxml.jackson.core:jackson-databind:2.x   <- Jackson 2
tools.jackson.core:jackson-databind:3.x           <- Jackson 3 (the mapper Spring MVC uses)
```

`jackson-databind-nullable` (0.2.6) is a **Jackson 2** library — its `JsonNullableModule` is a
`com.fasterxml.jackson.databind.Module`. Spring Boot 4 only wires **Jackson 3**
(`tools.jackson.databind.JacksonModule`) beans into the active mapper, so the module was silently
ignored and Jackson 3 had no (de)serializer for `JsonNullable` → the type-definition error. It was
not a configuration mistake; the library simply has no Jackson 3 build yet.

**Fix.** Replaced `JsonNullable` with a small native tri-state type and a Jackson 3 deserializer
(package `com.akydd.realworld_spring.json`):

- `Tristate<T>` — `undefined()` (absent) vs `of(value)` (present, value may be null).
- `TristateDeserializer` (`tools.jackson.databind.ValueDeserializer`) maps Jackson's parsing
  callbacks to the three states:
  - present value → `deserialize()` → `Tristate.of(value)`
  - explicit `null` → `getNullValue()` → `Tristate.of(null)` (clear)
  - absent property → `getAbsentValue()` → `Tristate.undefined()` (preserve)
- `TristateModule` registers it and is exposed as a `@Bean`.
- The `org.openapitools:jackson-databind-nullable` dependency was removed.

Empty-string → null normalization is applied when mapping the request DTO to the domain command
(`UserUpdateMapper`), keeping the raw request distinct from the business rule. Verified by
`TristateDeserializerTest`, which exercises all four cases through a real Jackson 3 mapper.

### Bean-validation on a method parameter changes the exception type

Putting a constraint annotation directly on a controller parameter
(`@Valid @NotNull @RequestBody ...`) switches Spring 6.1+/7 onto method-level validation, which
throws `HandlerMethodValidationException` (→ default 400) instead of
`MethodArgumentNotValidException` (→ the custom 422 handler). Dropping the redundant `@NotNull` and
keeping only `@Valid @RequestBody` restores the expected exception and the 422 response.

### Article tags: a unique-name many-to-many, without cascade surprises

**The setup.** An `Article` has a `@ManyToMany` to `Tag`; tags live in their own table and `tags.name`
is unique. Creating two articles that share a tag must **reuse** the existing `tags` row, never insert
a duplicate.

**Problem 1 — tags being (duplicately) persisted through the article.** With a `@ManyToMany`, two
traps lurk: if the mapper turns the request's `tagList` into `new Tag(name)` objects *and* the
association cascades `PERSIST`/`ALL`, saving the article re-inserts every tag — duplicating existing
names and violating the unique constraint; if it cascades but the tags are transient, you instead get
a "references an unsaved transient instance" error.

Fix — keep tag lifecycle out of both the entity and the mapper, and reuse existing rows:

- `Article.tags` has **no cascade**, so `articleRepository.save(article)` only writes `article_tags`
  join rows and never inserts into `tags` — the linked tags must already be persisted.
- `ArticleMapper.toEntity(...)` uses `@Mapping(target = "tags", ignore = true)`; the mapper never
  builds `Tag` entities. The tag *names* are passed to the service separately
  (`create(author, article, request.tagList())`).
- `ArticleServiceImpl.create` resolves names to managed tags with a get-or-create, after
  trim/blank-filter/`distinct`:

  ```java
  tagRepository.findByName(name).orElseGet(() -> tagRepository.save(new Tag(name)))
  ```

  Existing names are reused by reference; only genuinely new names are inserted.

Backstops: the `unique` constraint on `tags.name` (the source of truth), and `Tag.equals`/`hashCode`
based on `name` — a safe *immutable* natural key (unlike `username`/`email`), so the `HashSet<Tag>`
dedupes correctly across sessions. A concurrent-insert race on a brand-new tag name is still possible
and is not yet handled (would need catch-and-retry or an `INSERT ... ON CONFLICT` upsert). `Tag` also
needs a `protected` no-arg constructor alongside the `Tag(String)` convenience one — JPA requires it,
and declaring any constructor removes the implicit default.

**Problem 2 — tags saved but absent from `ArticleResponse`.** After a correct save, the response's
`tagList` came back empty. MapStruct could not auto-map `Article.tags` → `ArticleResponse.tagList`
because **both the name and the type differ**: `tags` vs `tagList`, and `Set<Tag>` vs `List<String>`.
It silently left the field unset. (This was *not* a lazy-loading problem — the service sets the tags
in memory before saving.)

Fix — declare both the rename and an element-conversion method:

```java
@Mapping(source = "tags", target = "tagList")
ArticleResponse toResponse(Article article);

default List<String> tagsToNames(Set<Tag> tags) {
    return tags == null ? new ArrayList<>() : tags.stream().map(Tag::getName).toList();
}
```

Related follow-up: the *update* path returns an article whose `tags` collection is lazy and never
touched, so mapping it renders only under open-session-in-view and would throw
`LazyInitializationException` if OSIV were disabled — a `JOIN FETCH` (or mapping inside the
transaction) is the durable fix.
