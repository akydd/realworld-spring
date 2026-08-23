package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** Mirrors {@code favorites.hurl}: favorite / unfavorite an article and list by favorited user. */
@DisplayName("Favorites API (favorites.hurl)")
class FavoritesApiTest extends ApiTestSupport {

    @Test
    @DisplayName("favorite and unfavorite an article, and filter articles by favorited user")
    void favoriteUnfavoriteAndFilter() {
        String uid = uid();
        String user = "fav_" + uid;
        String token = register(user, user + "@test.com", "password123");
        String slug = createArticle(token, "Favorite Article " + uid, "For favorites", "Article body");

        // favorite -> favorited true, count 1
        JsonNode favorited = body(expect(post("/api/articles/" + slug + "/favorite", null, token),
                HttpStatus.OK, "favorite")).path("article");
        assertArticleShape(favorited, "favorite response");
        assertThat(favorited.path("favorited").asBoolean()).as("favorite: favorited == true").isTrue();
        assertThat(favorited.path("favoritesCount").asInt()).as("favorite: favoritesCount == 1").isEqualTo(1);
        assertThat(favorited.path("author").path("username").asString()).as("favorite: author").isEqualTo(user);

        // favorite persists on GET
        JsonNode afterFav = body(expect(get("/api/articles/" + slug, token),
                HttpStatus.OK, "get after favorite")).path("article");
        assertThat(afterFav.path("favorited").asBoolean()).as("persisted: favorited == true").isTrue();
        assertThat(afterFav.path("favoritesCount").asInt()).as("persisted: favoritesCount == 1").isEqualTo(1);

        // list by favorited username, anonymously and authenticated — summary omits body
        for (String label : new String[]{"anonymous", "authenticated"}) {
            String tk = label.equals("authenticated") ? token : null;
            JsonNode list = body(expect(get("/api/articles?favorited=" + user, tk),
                    HttpStatus.OK, "list favorited (" + label + ")"));
            assertThat(list.path("articles").isArray()).as("%s: articles is a list", label).isTrue();
            assertThat(list.path("articlesCount").asInt()).as("%s: articlesCount >= 1", label).isGreaterThanOrEqualTo(1);
            JsonNode first = list.path("articles").path(0);
            assertThat(first.path("body").isMissingNode()).as("%s: list summary omits body", label).isTrue();
            assertThat(first.path("tagList").isArray()).as("%s: tagList is a list", label).isTrue();
            assertThat(first.path("favorited").isBoolean()).as("%s: favorited is boolean", label).isTrue();
            assertThat(first.path("favoritesCount").asInt()).as("%s: favoritesCount >= 1", label).isGreaterThanOrEqualTo(1);
        }

        // unfavorite -> favorited false, count 0
        JsonNode unfavorited = body(expect(delete("/api/articles/" + slug + "/favorite", token),
                HttpStatus.OK, "unfavorite")).path("article");
        assertThat(unfavorited.path("favorited").asBoolean()).as("unfavorite: favorited == false").isFalse();
        assertThat(unfavorited.path("favoritesCount").asInt()).as("unfavorite: favoritesCount == 0").isEqualTo(0);

        // unfavorite persists
        JsonNode afterUnfav = body(expect(get("/api/articles/" + slug, token),
                HttpStatus.OK, "get after unfavorite")).path("article");
        assertThat(afterUnfav.path("favorited").asBoolean()).as("persisted: favorited == false").isFalse();
        assertThat(afterUnfav.path("favoritesCount").asInt()).as("persisted: favoritesCount == 0").isEqualTo(0);

        expect(delete("/api/articles/" + slug, token), HttpStatus.NO_CONTENT, "cleanup: delete article");
    }

    private void assertArticleShape(JsonNode article, String step) {
        assertThat(article.path("title").isString()).as("%s: title is a string", step).isTrue();
        assertThat(article.path("slug").isString()).as("%s: slug is a string", step).isTrue();
        assertThat(article.path("description").isString()).as("%s: description is a string", step).isTrue();
        assertThat(article.path("body").isString()).as("%s: body is a string", step).isTrue();
        assertThat(article.path("tagList").isArray()).as("%s: tagList is a list", step).isTrue();
        assertThat(article.path("createdAt").asString()).as("%s: createdAt is ISO-8601", step).matches(ISO_TS);
        assertThat(article.path("updatedAt").asString()).as("%s: updatedAt is ISO-8601", step).matches(ISO_TS);
    }
}
