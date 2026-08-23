package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mirrors {@code errors_articles.hurl}: auth requirements, validation, unknown slugs, dup titles.
 */
@DisplayName("Article errors (errors_articles.hurl)")
class ErrorsArticlesApiTest extends ApiTestSupport {

    @Test
    @DisplayName("auth required (401), validation (422), unknown slugs (404), duplicate titles allowed")
    void articleErrors() {
        String uid = uid();

        // --- endpoints that require a token ---
        assertError(body(expect(post("/api/articles",
                        "{\"article\":{\"title\":\"No Auth Article\",\"description\":\"test\",\"body\":\"test\"}}", null),
                HttpStatus.UNAUTHORIZED, "create no auth")), "token", "is missing", "create no auth");
        assertError(body(expect(get("/api/articles/unknown-slug-" + uid, null),
                HttpStatus.NOT_FOUND, "GET unknown slug")), "article", "not found", "GET unknown slug");
        assertError(body(expect(put("/api/articles/some-slug", "{\"article\":{\"body\":\"test\"}}", null),
                HttpStatus.UNAUTHORIZED, "update no auth")), "token", "is missing", "update no auth");
        assertError(body(expect(delete("/api/articles/some-slug", null),
                HttpStatus.UNAUTHORIZED, "delete no auth")), "token", "is missing", "delete no auth");
        assertError(body(expect(get("/api/articles/feed", null),
                HttpStatus.UNAUTHORIZED, "feed no auth")), "token", "is missing", "feed no auth");
        assertError(body(expect(post("/api/articles/some-slug/favorite", null, null),
                HttpStatus.UNAUTHORIZED, "favorite no auth")), "token", "is missing", "favorite no auth");
        assertError(body(expect(delete("/api/articles/some-slug/favorite", null),
                HttpStatus.UNAUTHORIZED, "unfavorite no auth")), "token", "is missing", "unfavorite no auth");

        String user = "ea_art_" + uid;
        String token = register(user, user + "@test.com", "password123");

        // --- create validation ---
        assertError(body(expect(post("/api/articles", "{\"article\":{\"title\":\"\",\"description\":\"test\",\"body\":\"test\"}}", token),
                HttpStatus.UNPROCESSABLE_CONTENT, "empty title")), "title", "can't be blank", "empty title");
        assertError(body(expect(post("/api/articles", "{\"article\":{\"title\":\"Err Desc " + uid + "\",\"description\":\"\",\"body\":\"test\"}}", token),
                HttpStatus.UNPROCESSABLE_CONTENT, "empty description")), "description", "can't be blank", "empty description");
        assertError(body(expect(post("/api/articles", "{\"article\":{\"title\":\"Err Body " + uid + "\",\"description\":\"test\",\"body\":\"\"}}", token),
                HttpStatus.UNPROCESSABLE_CONTENT, "empty body")), "body", "can't be blank", "empty body");

        // --- duplicate titles allowed, distinct slugs ---
        String slug1 = body(expect(post("/api/articles", "{\"article\":{\"title\":\"Dup Title " + uid + "\",\"description\":\"first\",\"body\":\"first\"}}", token),
                HttpStatus.CREATED, "duplicate title #1")).path("article").path("slug").asString();
        String slug2 = body(expect(post("/api/articles", "{\"article\":{\"title\":\"Dup Title " + uid + "\",\"description\":\"second\",\"body\":\"second\"}}", token),
                HttpStatus.CREATED, "duplicate title #2")).path("article").path("slug").asString();
        assertThat(slug2).as("duplicate titles get distinct slugs").isNotEqualTo(slug1);

        // --- unknown slug operations -> 404 ---
        String unknown = "unknown-slug-" + uid;
        assertError(body(expect(put("/api/articles/" + unknown, "{\"article\":{\"body\":\"test\"}}", token),
                HttpStatus.NOT_FOUND, "update unknown slug")), "article", "not found", "update unknown slug");
        assertError(body(expect(post("/api/articles/" + unknown + "/favorite", null, token),
                HttpStatus.NOT_FOUND, "favorite unknown slug")), "article", "not found", "favorite unknown slug");
        assertError(body(expect(delete("/api/articles/" + unknown + "/favorite", token),
                HttpStatus.NOT_FOUND, "unfavorite unknown slug")), "article", "not found", "unfavorite unknown slug");
        assertError(body(expect(delete("/api/articles/" + unknown, token),
                HttpStatus.NOT_FOUND, "delete unknown slug")), "article", "not found", "delete unknown slug");

        // --- cleanup ---
        expect(delete("/api/articles/" + slug1, token), HttpStatus.NO_CONTENT, "cleanup slug1");
        expect(delete("/api/articles/" + slug2, token), HttpStatus.NO_CONTENT, "cleanup slug2");
    }
}
