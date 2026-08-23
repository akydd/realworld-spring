package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Mirrors {@code errors_comments.hurl}: auth, blank-body validation, and unknown article/comment.
 */
@DisplayName("Comment errors (errors_comments.hurl)")
class ErrorsCommentsApiTest extends ApiTestSupport {

    @Test
    @DisplayName("auth required (401), blank body (422), unknown article/comment (404)")
    void commentErrors() {
        String uid = uid();

        assertError(body(expect(post("/api/articles/some-slug/comments", "{\"comment\":{\"body\":\"test\"}}", null),
                HttpStatus.UNAUTHORIZED, "post comment without auth")), "token", "is missing", "post comment without auth");
        assertError(body(expect(delete("/api/articles/some-slug/comments/1", null),
                HttpStatus.UNAUTHORIZED, "delete comment without auth")), "token", "is missing", "delete comment without auth");

        String user = "ec_" + uid;
        String token = register(user, user + "@test.com", "password123");
        String slug = createArticle(token, "Err Comment Art " + uid, "test", "test");

        assertError(body(expect(post("/api/articles/" + slug + "/comments", "{\"comment\":{\"body\":\"\"}}", token),
                HttpStatus.UNPROCESSABLE_CONTENT, "blank comment body")), "body", "can't be blank", "blank comment body");

        String unknownSlug = "unknown-slug-" + uid;
        assertError(body(expect(post("/api/articles/" + unknownSlug + "/comments", "{\"comment\":{\"body\":\"orphan\"}}", token),
                HttpStatus.NOT_FOUND, "comment on unknown article")), "article", "not found", "comment on unknown article");
        assertError(body(expect(get("/api/articles/" + unknownSlug + "/comments", null),
                HttpStatus.NOT_FOUND, "list comments of unknown article")), "article", "not found", "list comments of unknown article");
        assertError(body(expect(delete("/api/articles/" + unknownSlug + "/comments/99999", token),
                HttpStatus.NOT_FOUND, "delete comment of unknown article")), "article", "not found", "delete comment of unknown article");
        assertError(body(expect(delete("/api/articles/" + slug + "/comments/99999", token),
                HttpStatus.NOT_FOUND, "delete unknown comment")), "comment", "not found", "delete unknown comment");

        expect(delete("/api/articles/" + slug, token), HttpStatus.NO_CONTENT, "cleanup");
    }
}
