package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** Mirrors {@code comments.hurl}: create, list (auth + anon), delete, and selective deletion. */
@DisplayName("Comments API (comments.hurl)")
class CommentsApiTest extends ApiTestSupport {

    @Test
    @DisplayName("comment lifecycle: create, list, delete, and delete-one-of-two")
    void commentLifecycle() {
        String uid = uid();
        String author = "cmt_" + uid;
        String token = register(author, author + "@test.com", "password123");
        String slug = createArticle(token, "Comment Article " + uid, "For comments", "Article body");

        // create a comment
        JsonNode comment = body(expect(
                post("/api/articles/" + slug + "/comments", """
                        {"comment":{"body":"Test comment body"}}""", token),
                HttpStatus.CREATED, "create comment")).path("comment");
        assertThat(comment.path("id").isIntegralNumber()).as("comment.id is an integer").isTrue();
        assertThat(comment.path("body").asString()).as("comment.body").isEqualTo("Test comment body");
        assertThat(comment.path("createdAt").asString()).as("comment.createdAt is ISO-8601").matches(ISO_TS);
        assertThat(comment.path("updatedAt").asString()).as("comment.updatedAt is ISO-8601").matches(ISO_TS);
        assertThat(comment.path("author").path("username").asString()).as("comment.author.username").isEqualTo(author);
        long commentId = comment.path("id").asLong();

        // list comments (authenticated)
        JsonNode listAuth = body(expect(get("/api/articles/" + slug + "/comments", token),
                HttpStatus.OK, "list comments (auth)")).path("comments");
        assertThat(listAuth.isArray()).as("auth list: comments is a list").isTrue();
        assertThat(listAuth.size()).as("auth list: exactly one comment").isEqualTo(1);
        assertThat(listAuth.path(0).path("id").asLong()).as("auth list: comment id matches").isEqualTo(commentId);
        assertThat(listAuth.path(0).path("body").asString()).as("auth list: comment body").isEqualTo("Test comment body");
        assertThat(listAuth.path(0).path("author").path("username").asString()).as("auth list: author").isEqualTo(author);

        // list comments (anonymous)
        JsonNode listAnon = body(expect(get("/api/articles/" + slug + "/comments", null),
                HttpStatus.OK, "list comments (anon)")).path("comments");
        assertThat(listAnon.size()).as("anon list: exactly one comment").isEqualTo(1);
        assertThat(listAnon.path(0).path("body").asString()).as("anon list: comment body").isEqualTo("Test comment body");

        // delete the comment
        expect(delete("/api/articles/" + slug + "/comments/" + commentId, token), HttpStatus.NO_CONTENT, "delete comment");
        assertThat(body(expect(get("/api/articles/" + slug + "/comments", null), HttpStatus.OK, "list after delete"))
                .path("comments").size()).as("after delete: zero comments").isEqualTo(0);

        // selective deletion: create two, delete the first, the second remains
        long firstId = createComment(slug, token, "First comment");
        createComment(slug, token, "Second comment");
        assertThat(body(expect(get("/api/articles/" + slug + "/comments", null), HttpStatus.OK, "list two"))
                .path("comments").size()).as("two comments exist").isEqualTo(2);

        expect(delete("/api/articles/" + slug + "/comments/" + firstId, token), HttpStatus.NO_CONTENT, "delete first comment");
        JsonNode remaining = body(expect(get("/api/articles/" + slug + "/comments", null), HttpStatus.OK, "list remaining"))
                .path("comments");
        assertThat(remaining.size()).as("one comment remains").isEqualTo(1);
        assertThat(remaining.path(0).path("body").asString()).as("the remaining comment is the second").isEqualTo("Second comment");

        expect(delete("/api/articles/" + slug, token), HttpStatus.NO_CONTENT, "cleanup: delete article");
    }

    private long createComment(String slug, String token, String text) {
        JsonNode c = body(expect(post("/api/articles/" + slug + "/comments",
                """
                {"comment":{"body":"%s"}}""".formatted(text), token),
                HttpStatus.CREATED, "create comment '" + text + "'")).path("comment");
        return c.path("id").asLong();
    }
}
