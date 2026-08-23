package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mirrors {@code errors_authorization.hurl}: only owners may modify their articles and comments.
 */
@DisplayName("Authorization errors (errors_authorization.hurl)")
class ErrorsAuthorizationApiTest extends ApiTestSupport {

    @Test
    @DisplayName("a non-owner gets 403 updating/deleting another's article or deleting their comment")
    void ownershipEnforced() {
        String uid = uid();
        String a = "authz_a_" + uid;
        String b = "authz_b_" + uid;
        String tokenA = register(a, a + "@test.com", "password123");
        String tokenB = register(b, b + "@test.com", "password123");

        String slug = createArticle(tokenA, "Authz Article " + uid, "test", "test");

        // B cannot delete or update A's article
        assertError(body(expect(delete("/api/articles/" + slug, tokenB),
                HttpStatus.FORBIDDEN, "B deletes A's article")), "article", "forbidden", "B deletes A's article");
        assertError(body(expect(put("/api/articles/" + slug, "{\"article\":{\"body\":\"hijacked\"}}", tokenB),
                HttpStatus.FORBIDDEN, "B updates A's article")), "article", "forbidden", "B updates A's article");

        // A comments; B cannot delete A's comment
        long commentId = body(expect(post("/api/articles/" + slug + "/comments",
                        "{\"comment\":{\"body\":\"A's comment\"}}", tokenA),
                HttpStatus.CREATED, "A comments")).path("comment").path("id").asLong();
        assertError(body(expect(delete("/api/articles/" + slug + "/comments/" + commentId, tokenB),
                HttpStatus.FORBIDDEN, "B deletes A's comment")), "comment", "forbidden", "B deletes A's comment");

        // the comment survived the failed delete
        JsonNode comments = body(expect(get("/api/articles/" + slug + "/comments", null),
                HttpStatus.OK, "list comments after failed delete")).path("comments");
        assertThat(comments.size()).as("comment survived the failed delete").isGreaterThanOrEqualTo(1);
        assertThat(comments.path(0).path("body").asString()).as("surviving comment body").isEqualTo("A's comment");

        expect(delete("/api/articles/" + slug, tokenA), HttpStatus.NO_CONTENT, "cleanup: owner deletes article");
    }
}
