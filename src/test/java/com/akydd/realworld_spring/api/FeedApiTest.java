package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mirrors {@code feed.hurl}: the feed reflects followed authors' articles, with paging.
 */
@DisplayName("Feed API (feed.hurl)")
class FeedApiTest extends ApiTestSupport {

    @Test
    @DisplayName("a followed author's articles appear in the feed, with limit/offset paging")
    void feedOfFollowedAuthors() {
        String uid = uid();
        String main = "feedm_" + uid;
        String celeb = "feedc_" + uid;
        String mainToken = register(main, main + "@test.com", "password123");
        String celebToken = register(celeb, celeb + "@test.com", "password123");

        // a brand-new user's feed is empty
        JsonNode empty = body(expect(get("/api/articles/feed", mainToken), HttpStatus.OK, "empty feed"));
        assertThat(empty.path("articlesCount").asInt()).as("new user feed: articlesCount == 0").isEqualTo(0);
        assertThat(empty.path("articles").size()).as("new user feed: no articles").isEqualTo(0);

        // main follows celeb
        JsonNode follow = body(expect(post("/api/profiles/" + celeb + "/follow", null, mainToken),
                HttpStatus.OK, "follow celeb"));
        assertThat(follow.path("profile").path("following").asBoolean()).as("follow: following == true").isTrue();

        // celeb writes two articles
        String slug1 = createArticle(celebToken, "Feed Article 1 " + uid, "Feed test 1", "Feed body 1");
        String slug2 = createArticle(celebToken, "Feed Article 2 " + uid, "Feed test 2", "Feed body 2");

        // main's feed now has both
        JsonNode feed = body(expect(get("/api/articles/feed", mainToken), HttpStatus.OK, "feed"));
        assertThat(feed.path("articles").isArray()).as("feed: articles is a list").isTrue();
        assertThat(feed.path("articlesCount").asInt()).as("feed: articlesCount == 2").isEqualTo(2);
        assertThat(feed.path("articles").size()).as("feed: two articles").isEqualTo(2);
        JsonNode first = feed.path("articles").path(0);
        assertThat(first.path("body").isMissingNode()).as("feed summary omits body").isTrue();
        assertThat(first.path("tagList").isArray()).as("feed: tagList is a list").isTrue();
        assertThat(first.path("createdAt").asString()).as("feed: createdAt is ISO-8601").matches(ISO_TS);
        assertThat(first.path("favorited").isBoolean()).as("feed: favorited is boolean").isTrue();
        assertThat(first.path("author").path("username").asString()).as("feed: author is the celeb").isEqualTo(celeb);

        // feed limit=1 — the author is one this user follows
        JsonNode limited = body(expect(get("/api/articles/feed?limit=1", mainToken), HttpStatus.OK, "feed limit=1"));
        assertThat(limited.path("articles").size()).as("feed limit=1: one article").isEqualTo(1);
        assertThat(limited.path("articlesCount").asInt()).as("feed limit=1: total still 2").isEqualTo(2);
        assertThat(limited.path("articles").path(0).path("author").path("following").asBoolean())
                .as("feed limit=1: author.following == true").isTrue();

        // feed limit=1&offset=1
        JsonNode paged = body(expect(get("/api/articles/feed?limit=1&offset=1", mainToken), HttpStatus.OK, "feed offset"));
        assertThat(paged.path("articles").size()).as("feed offset: one article").isEqualTo(1);
        assertThat(paged.path("articlesCount").asInt()).as("feed offset: total still 2").isEqualTo(2);

        // cleanup
        expect(delete("/api/articles/" + slug1, celebToken), HttpStatus.NO_CONTENT, "cleanup: delete slug1");
        expect(delete("/api/articles/" + slug2, celebToken), HttpStatus.NO_CONTENT, "cleanup: delete slug2");
        expect(delete("/api/profiles/" + celeb + "/follow", mainToken), HttpStatus.OK, "cleanup: unfollow");
    }
}
