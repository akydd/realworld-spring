# realworld-spring

A Spring Boot implementation of the [RealWorld](https://realworld-docs.netlify.app/specifications/backend/endpoints/)
("Conduit") backend API.

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

1. Start a PostgreSQL instance matching `src/main/resources/application.properties`:

   ```
   host: localhost:8095
   database: app
   user: admin
   password: password
   ```

2. Run the app (Flyway applies migrations from `src/main/resources/db/migration` on startup):

   ```bash
   ./gradlew bootRun
   ```

   The API is served at `http://localhost:8080`.

3. Run the unit tests:

   ```bash
   ./gradlew test
   ```

The RealWorld spec's end-to-end tests (Hurl) live outside this repo under
`../realworld/specs/api/hurl` and can be run with the `run-hurl-tests.sh` script there against a
running instance.

## Authentication notes

- The API expects the RealWorld auth scheme: `Authorization: Token <jwt>` (not `Bearer`).
- The JWT **subject is the immutable user id**, deliberately *not* the username or email — the spec
  allows a user to change both via `PUT /api/user`, and putting a mutable value in the token would
  invalidate outstanding tokens (or, worse, let a recycled name resolve to a different user) after a
  rename. Login authenticates by email; the token then carries the id; the auth filter resolves the
  caller by id.

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
