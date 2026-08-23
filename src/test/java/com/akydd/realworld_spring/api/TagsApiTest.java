package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** Mirrors {@code tags.hurl}: tags used by articles surface via GET /api/tags. */
@DisplayName("Tags API (tags.hurl)")
class TagsApiTest extends ApiTestSupport {

    @Test
    @DisplayName("an article's tags appear in GET /api/tags")
    void articleTagsAppearInTagList() {
        String uid = uid();
        String token = register("tag_" + uid, "tag_" + uid + "@test.com", "password123");
        String hTag = "h_" + uid;
        String tTag = "t_" + uid;

        ResponseEntity<String> create = post("/api/articles", """
                {"article":{"title":"Tag Article %s","description":"For tags","body":"Article body",\
                "tagList":["%s","%s"]}}""".formatted(uid, hTag, tTag), token);
        assertThat(create.getStatusCode())
                .as("create tagged article should return 201 (body: %s)", create.getBody())
                .isEqualTo(HttpStatus.CREATED);
        String slug = body(create).path("article").path("slug").asString();

        ResponseEntity<String> tags = get("/api/tags", null);
        assertThat(tags.getStatusCode()).as("GET /api/tags status").isEqualTo(HttpStatus.OK);

        JsonNode tagList = body(tags).path("tags");
        assertThat(tagList.isArray()).as("$.tags is a list").isTrue();
        assertThat(tagList.size()).as("$.tags has at least one entry").isGreaterThanOrEqualTo(1);
        assertThat(strings(tagList))
                .as("$.tags contains both of the new article's tags")
                .contains(hTag, tTag);
        assertThat(tagList.path(0).isString()).as("$.tags[0] is a string").isTrue();

        ResponseEntity<String> cleanup = delete("/api/articles/" + slug, token);
        assertThat(cleanup.getStatusCode())
                .as("cleanup: delete article should return 204")
                .isEqualTo(HttpStatus.NO_CONTENT);
    }
}
