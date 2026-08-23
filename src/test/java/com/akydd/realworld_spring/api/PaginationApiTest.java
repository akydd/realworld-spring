package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mirrors {@code pagination.hurl}: limit/offset paging over an author's articles, newest first.
 */
@DisplayName("Pagination API (pagination.hurl)")
class PaginationApiTest extends ApiTestSupport {

    @Test
    @DisplayName("limit and offset page through an author's articles, most recent first")
    void limitAndOffsetPaging() {
        String uid = uid();
        String author = "page_" + uid;
        String token = register(author, author + "@test.com", "password123");

        String slug1 = createArticle(token, "Pagination 1 " + uid, "Page test 1", "Page body 1");
        String slug2 = createArticle(token, "Pagination 2 " + uid, "Page test 2", "Page body 2");

        // First page (limit=1): newest article (slug2).
        ResponseEntity<String> page1 = get("/api/articles?author=" + author + "&limit=1", null);
        assertThat(page1.getStatusCode()).as("list page 1 status").isEqualTo(HttpStatus.OK);
        JsonNode p1 = body(page1);
        assertThat(p1.path("articles").size()).as("page 1: articles count").isEqualTo(1);
        assertThat(p1.path("articlesCount").asInt()).as("page 1: total articlesCount").isEqualTo(2);
        assertThat(p1.path("articles").path(0).path("slug").asString())
                .as("page 1: first article is the most recent (slug2)").isEqualTo(slug2);

        // Second page (limit=1&offset=1): older article (slug1).
        ResponseEntity<String> page2 = get("/api/articles?author=" + author + "&limit=1&offset=1", null);
        assertThat(page2.getStatusCode()).as("list page 2 status").isEqualTo(HttpStatus.OK);
        JsonNode p2 = body(page2);
        assertThat(p2.path("articles").size()).as("page 2: articles count").isEqualTo(1);
        assertThat(p2.path("articlesCount").asInt()).as("page 2: total articlesCount").isEqualTo(2);
        assertThat(p2.path("articles").path(0).path("slug").asString())
                .as("page 2: article is the older one (slug1)").isEqualTo(slug1);

        assertThat(delete("/api/articles/" + slug1, token).getStatusCode())
                .as("cleanup: delete slug1").isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(delete("/api/articles/" + slug2, token).getStatusCode())
                .as("cleanup: delete slug2").isEqualTo(HttpStatus.NO_CONTENT);
    }
}
