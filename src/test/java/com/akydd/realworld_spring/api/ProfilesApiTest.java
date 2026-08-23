package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** Mirrors {@code profiles.hurl}: viewing a profile (with and without auth) and follow/unfollow. */
@DisplayName("Profiles API (profiles.hurl)")
class ProfilesApiTest extends ApiTestSupport {

    @Test
    @DisplayName("view profile anonymously and authenticated, then follow / unfollow a user")
    void viewFollowUnfollow() {
        String uid = uid();
        String token = register("prof_" + uid, "prof_" + uid + "@test.com", "password123");
        String celeb = "celeb_" + uid;
        register(celeb, celeb + "@test.com", "password123");

        // Anonymous view: following must be false.
        assertProfile(get("/api/profiles/" + celeb, null), celeb, false, "anonymous GET profile");

        // Authenticated view (not yet following): still false.
        assertProfile(get("/api/profiles/" + celeb, token), celeb, false, "authenticated GET profile");

        // Follow -> following true.
        assertProfile(post("/api/profiles/" + celeb + "/follow", null, token), celeb, true, "POST follow");

        // Unfollow -> following false.
        assertProfile(delete("/api/profiles/" + celeb + "/follow", token), celeb, false, "DELETE follow");

        // Unfollow persisted.
        assertProfile(get("/api/profiles/" + celeb, token), celeb, false, "GET profile after unfollow");
    }

    private void assertProfile(ResponseEntity<String> response, String username, boolean following, String step) {
        assertThat(response.getStatusCode())
                .as("%s: status should be 200 (body: %s)", step, response.getBody())
                .isEqualTo(HttpStatus.OK);
        JsonNode profile = body(response).path("profile");
        assertThat(profile.path("username").asString())
                .as("%s: profile.username", step).isEqualTo(username);
        assertThat(profile.path("bio").isNull())
                .as("%s: profile.bio should be null", step).isTrue();
        assertThat(profile.path("image").isNull())
                .as("%s: profile.image should be null", step).isTrue();
        assertThat(profile.path("following").asBoolean())
                .as("%s: profile.following should be %s", step, following).isEqualTo(following);
    }
}
