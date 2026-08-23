package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/** Mirrors {@code auth.hurl}: register, login, current user, and PUT /user (bio/image tri-state). */
@DisplayName("Auth & current-user API (auth.hurl)")
class AuthApiTest extends ApiTestSupport {

    @Test
    @DisplayName("register, login, get current user, update bio/image (absent/empty/null), rename")
    void authAndUserUpdates() {
        String uid = uid();
        String username = "auth_" + uid;
        String email = username + "@test.com";

        // --- register ---
        JsonNode reg = body(expect(post("/api/users",
                """
                {"user":{"username":"%s","email":"%s","password":"password123"}}""".formatted(username, email), null),
                HttpStatus.CREATED, "register")).path("user");
        assertThat(reg.path("username").asString()).as("register: username").isEqualTo(username);
        assertThat(reg.path("email").asString()).as("register: email").isEqualTo(email);
        assertThat(reg.path("bio").isNull()).as("register: bio is null").isTrue();
        assertThat(reg.path("image").isNull()).as("register: image is null").isTrue();
        assertThat(reg.path("token").asString()).as("register: token is non-empty").isNotBlank();

        // --- login ---
        JsonNode login = body(expect(post("/api/users/login",
                """
                {"user":{"email":"%s","password":"password123"}}""".formatted(email), null),
                HttpStatus.OK, "login")).path("user");
        assertThat(login.path("username").asString()).as("login: username").isEqualTo(username);
        assertThat(login.path("email").asString()).as("login: email").isEqualTo(email);
        assertThat(login.path("bio").isNull()).as("login: bio is null").isTrue();
        assertThat(login.path("image").isNull()).as("login: image is null").isTrue();
        String token = login.path("token").asString();
        assertThat(token).as("login: token is non-empty").isNotBlank();

        // --- get current user ---
        JsonNode me = currentUser(token);
        assertThat(me.path("username").asString()).as("current user: username").isEqualTo(username);
        assertThat(me.path("email").asString()).as("current user: email").isEqualTo(email);
        assertThat(me.path("bio").isNull()).as("current user: bio is null").isTrue();

        // --- bio: set, then empty-string normalizes to null, then null clears ---
        assertThat(updateUser(token, "{\"bio\":\"Updated bio\"}").path("bio").asString())
                .as("update bio -> 'Updated bio'").isEqualTo("Updated bio");
        assertThat(currentUser(token).path("bio").asString()).as("persisted: bio == 'Updated bio'").isEqualTo("Updated bio");

        assertThat(updateUser(token, "{\"bio\":\"\"}").path("bio").isNull())
                .as("update bio '' -> normalized to null").isTrue();
        assertThat(currentUser(token).path("bio").isNull()).as("persisted: bio null after empty string").isTrue();

        assertThat(updateUser(token, "{\"bio\":\"Temporary bio\"}").path("bio").asString())
                .as("restore bio -> 'Temporary bio'").isEqualTo("Temporary bio");
        assertThat(updateUser(token, "{\"bio\":null}").path("bio").isNull())
                .as("update bio null -> cleared").isTrue();
        assertThat(currentUser(token).path("bio").isNull()).as("persisted: bio null after explicit null").isTrue();

        assertThat(updateUser(token, "{\"bio\":\"Updated bio\"}").path("bio").asString())
                .as("restore bio -> 'Updated bio'").isEqualTo("Updated bio");

        // --- image: set, empty-string -> null, null clears ---
        assertThat(updateUser(token, "{\"image\":\"https://example.com/photo.jpg\"}").path("image").asString())
                .as("update image").isEqualTo("https://example.com/photo.jpg");
        assertThat(currentUser(token).path("image").asString()).as("persisted: image").isEqualTo("https://example.com/photo.jpg");

        assertThat(updateUser(token, "{\"image\":\"\"}").path("image").isNull())
                .as("update image '' -> normalized to null").isTrue();
        assertThat(currentUser(token).path("image").isNull()).as("persisted: image null after empty string").isTrue();

        assertThat(updateUser(token, "{\"image\":\"https://example.com/temp.jpg\"}").path("image").asString())
                .as("set temp image").isEqualTo("https://example.com/temp.jpg");
        assertThat(updateUser(token, "{\"image\":null}").path("image").isNull())
                .as("update image null -> cleared").isTrue();
        assertThat(currentUser(token).path("image").isNull()).as("persisted: image null after explicit null").isTrue();

        // --- rename username + email; a token comes back and stays valid ---
        String newUsername = username + "_upd";
        String newEmail = username + "_upd@test.com";
        JsonNode renamed = updateUser(token,
                "{\"username\":\"" + newUsername + "\",\"email\":\"" + newEmail + "\"}");
        assertThat(renamed.path("username").asString()).as("rename: username").isEqualTo(newUsername);
        assertThat(renamed.path("email").asString()).as("rename: email").isEqualTo(newEmail);
        assertThat(renamed.path("bio").asString()).as("rename: bio preserved").isEqualTo("Updated bio");
        assertThat(renamed.path("image").isNull()).as("rename: image still null").isTrue();
        String updatedToken = renamed.path("token").asString();
        assertThat(updatedToken).as("rename: token is non-empty").isNotBlank();

        JsonNode afterRename = currentUser(updatedToken);
        assertThat(afterRename.path("username").asString()).as("persisted rename: username").isEqualTo(newUsername);
        assertThat(afterRename.path("email").asString()).as("persisted rename: email").isEqualTo(newEmail);
        assertThat(afterRename.path("bio").asString()).as("persisted rename: bio").isEqualTo("Updated bio");
    }

    /** PUT /api/user with the given inner user fields (wrapped in {"user":...}); returns $.user. */
    private JsonNode updateUser(String token, String userFieldsJson) {
        return body(expect(put("/api/user", "{\"user\":" + userFieldsJson + "}", token),
                HttpStatus.OK, "PUT /api/user " + userFieldsJson)).path("user");
    }

    private JsonNode currentUser(String token) {
        return body(expect(get("/api/user", token), HttpStatus.OK, "GET /api/user")).path("user");
    }
}
