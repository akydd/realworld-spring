package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** Mirrors {@code articles.hurl}: create, list (author/tag filters), read, update, and delete. */
@DisplayName("Articles API (articles.hurl)")
class ArticlesApiTest extends ApiTestSupport {

    @Test
    @DisplayName("create with tags, list with filters, read, update (preserve/clear/reject tags), delete")
    void articleCrudAndListing() {
        String uid = uid();
        String author = "art_" + uid;
        String token = register(author, author + "@test.com", "password123");
        String dTag = "d_" + uid;
        String tTag = "t_" + uid;

        // --- create with tags ---
        JsonNode created = body(expect(post("/api/articles", """
                {"article":{"title":"Test Article %s","description":"Test description","body":"Test body content","tagList":["%s","%s"]}}"""
                .formatted(uid, dTag, tTag), token), HttpStatus.CREATED, "create article")).path("article");
        assertThat(created.path("title").asString()).as("create: title").isEqualTo("Test Article " + uid);
        assertThat(created.path("slug").isString()).as("create: slug is a string").isTrue();
        assertThat(created.path("description").asString()).as("create: description").isEqualTo("Test description");
        assertThat(created.path("body").asString()).as("create: body").isEqualTo("Test body content");
        assertThat(strings(created.path("tagList"))).as("create: tagList in order").containsExactly(dTag, tTag);
        assertThat(created.path("createdAt").asString()).as("create: createdAt is ISO-8601").matches(ISO_TS);
        assertThat(created.path("favorited").asBoolean()).as("create: favorited == false").isFalse();
        assertThat(created.path("favoritesCount").asInt()).as("create: favoritesCount == 0").isEqualTo(0);
        assertThat(created.path("author").path("username").asString()).as("create: author").isEqualTo(author);
        String slug = created.path("slug").asString();
        String createdAt = created.path("createdAt").asString();
        String updatedAt = created.path("updatedAt").asString();

        // --- list all (summary shape) ---
        JsonNode all = body(expect(get("/api/articles", null), HttpStatus.OK, "list all"));
        assertThat(all.path("articles").isArray()).as("list all: articles is a list").isTrue();
        assertThat(all.path("articlesCount").asInt()).as("list all: articlesCount >= 1").isGreaterThanOrEqualTo(1);
        assertSummaryShape(all.path("articles").path(0), "list all");

        // --- list by author (anon + auth) ---
        JsonNode byAuthor = body(expect(get("/api/articles?author=" + author, null), HttpStatus.OK, "list by author"));
        assertThat(byAuthor.path("articlesCount").asInt()).as("by author: count >= 1").isGreaterThanOrEqualTo(1);
        assertThat(byAuthor.path("articles").path(0).path("author").path("username").asString())
                .as("by author: author matches").isEqualTo(author);
        assertSummaryShape(byAuthor.path("articles").path(0), "list by author");

        assertSummaryShape(body(expect(get("/api/articles", token), HttpStatus.OK, "list all (auth)"))
                .path("articles").path(0), "list all (auth)");
        assertThat(body(expect(get("/api/articles?author=" + author, token), HttpStatus.OK, "list by author (auth)"))
                .path("articles").path(0).path("author").path("username").asString())
                .as("by author (auth): author matches").isEqualTo(author);

        // --- list by tag ---
        JsonNode byTag = body(expect(get("/api/articles?tag=" + dTag, null), HttpStatus.OK, "list by tag"));
        assertThat(byTag.path("articlesCount").asInt()).as("by tag: count >= 1").isGreaterThanOrEqualTo(1);
        assertThat(strings(byTag.path("articles").path(0).path("tagList")))
                .as("by tag: article carries the tag").contains(dTag);
        assertSummaryShape(byTag.path("articles").path(0), "list by tag");

        // --- get single (full shape) ---
        JsonNode single = body(expect(get("/api/articles/" + slug, null), HttpStatus.OK, "get single")).path("article");
        assertThat(single.path("slug").asString()).as("single: slug").isEqualTo(slug);
        assertThat(single.path("body").asString()).as("single: body").isEqualTo("Test body content");
        assertThat(single.path("favorited").asBoolean()).as("single: favorited == false").isFalse();
        assertThat(single.path("author").path("username").asString()).as("single: author").isEqualTo(author);

        // --- update body: createdAt unchanged, updatedAt changed, tags preserved ---
        JsonNode updated = body(expect(put("/api/articles/" + slug, """
                {"article":{"body":"Updated body content"}}""", token), HttpStatus.OK, "update body")).path("article");
        assertThat(updated.path("title").asString()).as("update: title unchanged").isEqualTo("Test Article " + uid);
        assertThat(updated.path("body").asString()).as("update: body updated").isEqualTo("Updated body content");
        assertThat(updated.path("tagList").size()).as("update: 2 tags preserved").isEqualTo(2);
        assertThat(strings(updated.path("tagList"))).as("update: tags preserved").contains(dTag, tTag);
        assertThat(updated.path("createdAt").asString()).as("update: createdAt unchanged").isEqualTo(createdAt);
        assertThat(updated.path("updatedAt").asString()).as("update: updatedAt changed").isNotEqualTo(updatedAt);

        assertThat(body(expect(get("/api/articles/" + slug, null), HttpStatus.OK, "get after update"))
                .path("article").path("body").asString()).as("persisted: body updated").isEqualTo("Updated body content");

        // --- update without tagList: tags preserved ---
        JsonNode keptTags = body(expect(put("/api/articles/" + slug, """
                {"article":{"body":"Body without touching tags"}}""", token), HttpStatus.OK, "update (no tagList)")).path("article");
        assertThat(keptTags.path("body").asString()).as("no-tagList update: body").isEqualTo("Body without touching tags");
        assertThat(keptTags.path("tagList").size()).as("no-tagList update: tags preserved").isEqualTo(2);

        // --- update with empty tagList: tags removed ---
        JsonNode clearedTags = body(expect(put("/api/articles/" + slug, """
                {"article":{"tagList":[]}}""", token), HttpStatus.OK, "update (empty tagList)")).path("article");
        assertThat(clearedTags.path("tagList").isArray()).as("empty tagList: still a list").isTrue();
        assertThat(clearedTags.path("tagList").size()).as("empty tagList: zero tags").isEqualTo(0);
        assertThat(body(expect(get("/api/articles/" + slug, null), HttpStatus.OK, "get after clearing tags"))
                .path("article").path("tagList").size()).as("persisted: zero tags").isEqualTo(0);

        // --- update with tagList null: rejected ---
        expect(put("/api/articles/" + slug, """
                {"article":{"tagList":null}}""", token), HttpStatus.UNPROCESSABLE_CONTENT, "update (tagList null) rejected");

        // --- delete, then confirm 404 with the documented error shape ---
        expect(delete("/api/articles/" + slug, token), HttpStatus.NO_CONTENT, "delete article");
        JsonNode notFound = body(expect(get("/api/articles/" + slug, null), HttpStatus.NOT_FOUND, "get deleted article -> 404"));
        assertThat(notFound.path("errors").path("article").path(0).asString())
                .as("deleted: errors.article[0] == 'not found'").isEqualTo("not found");
    }

    private void assertSummaryShape(JsonNode summary, String step) {
        assertThat(summary.path("title").isString()).as("%s: title is a string", step).isTrue();
        assertThat(summary.path("slug").isString()).as("%s: slug is a string", step).isTrue();
        assertThat(summary.path("description").isString()).as("%s: description is a string", step).isTrue();
        assertThat(summary.path("body").isMissingNode()).as("%s: summary omits body", step).isTrue();
        assertThat(summary.path("tagList").isArray()).as("%s: tagList is a list", step).isTrue();
        assertThat(summary.path("createdAt").asString()).as("%s: createdAt is ISO-8601", step).matches(ISO_TS);
        assertThat(summary.path("favorited").isBoolean()).as("%s: favorited is boolean", step).isTrue();
        assertThat(summary.path("favoritesCount").isIntegralNumber()).as("%s: favoritesCount is integer", step).isTrue();
    }
}
