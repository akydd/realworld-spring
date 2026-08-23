package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Mirrors {@code errors_profiles.hurl}: unknown profiles, and follow/unfollow auth/404 errors. */
@DisplayName("Profile errors (errors_profiles.hurl)")
class ErrorsProfilesApiTest extends ApiTestSupport {

    @Test
    @DisplayName("unknown profile 404; follow/unfollow require auth (401) and target must exist (404)")
    void profileErrors() {
        String uid = uid();
        String unknown = "unknown-user-" + uid;

        assertError(body(expect(get("/api/profiles/" + unknown, null),
                HttpStatus.NOT_FOUND, "GET unknown profile")), "profile", "not found", "GET unknown profile");

        assertError(body(expect(post("/api/profiles/" + unknown + "/follow", null, null),
                HttpStatus.UNAUTHORIZED, "follow without auth")), "token", "is missing", "follow without auth");
        assertError(body(expect(delete("/api/profiles/" + unknown + "/follow", null),
                HttpStatus.UNAUTHORIZED, "unfollow without auth")), "token", "is missing", "unfollow without auth");

        String user = "ep_" + uid;
        String token = register(user, user + "@test.com", "password123");

        assertError(body(expect(post("/api/profiles/" + unknown + "/follow", null, token),
                HttpStatus.NOT_FOUND, "follow unknown (authed)")), "profile", "not found", "follow unknown (authed)");
        assertError(body(expect(delete("/api/profiles/" + unknown + "/follow", token),
                HttpStatus.NOT_FOUND, "unfollow unknown (authed)")), "profile", "not found", "unfollow unknown (authed)");
    }
}
