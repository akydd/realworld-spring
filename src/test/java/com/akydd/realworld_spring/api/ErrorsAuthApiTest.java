package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/** Mirrors {@code errors_auth.hurl}: registration/login validation, duplicates, and PUT /user rules. */
@DisplayName("Auth errors (errors_auth.hurl)")
class ErrorsAuthApiTest extends ApiTestSupport {

    @Test
    @DisplayName("blank/duplicate registration, login credentials, and PUT /user field + password rules")
    void authErrors() {
        String uid = uid();

        // --- registration validation ---
        assertError(body(expect(post("/api/users",
                "{\"user\":{\"username\":\"\",\"email\":\"ea_blank_" + uid + "@test.com\",\"password\":\"password123\"}}", null),
                HttpStatus.UNPROCESSABLE_CONTENT, "register blank username")), "username", "can't be blank", "register blank username");
        assertError(body(expect(post("/api/users",
                "{\"user\":{\"username\":\"ea_blank_" + uid + "\",\"email\":\"\",\"password\":\"password123\"}}", null),
                HttpStatus.UNPROCESSABLE_CONTENT, "register blank email")), "email", "can't be blank", "register blank email");
        assertError(body(expect(post("/api/users",
                "{\"user\":{\"username\":\"ea_blankp_" + uid + "\",\"email\":\"ea_blankp_" + uid + "@test.com\",\"password\":\"\"}}", null),
                HttpStatus.UNPROCESSABLE_CONTENT, "register blank password")), "password", "can't be blank", "register blank password");

        // --- a valid user, then duplicate username / email -> 409 ---
        String dupUser = "ea_dup_" + uid;
        String dupEmail = "ea_dup_" + uid + "@test.com";
        String token = register(dupUser, dupEmail, "password123");

        assertError(body(expect(post("/api/users",
                "{\"user\":{\"username\":\"" + dupUser + "\",\"email\":\"ea_dup2_" + uid + "@test.com\",\"password\":\"password123\"}}", null),
                HttpStatus.CONFLICT, "duplicate username")), "username", "has already been taken", "duplicate username");
        assertError(body(expect(post("/api/users",
                "{\"user\":{\"username\":\"ea_dup2_" + uid + "\",\"email\":\"" + dupEmail + "\",\"password\":\"password123\"}}", null),
                HttpStatus.CONFLICT, "duplicate email")), "email", "has already been taken", "duplicate email");

        // --- login validation + wrong credentials ---
        assertError(body(expect(post("/api/users/login", "{\"user\":{\"email\":\"\",\"password\":\"password123\"}}", null),
                HttpStatus.UNPROCESSABLE_CONTENT, "login blank email")), "email", "can't be blank", "login blank email");
        assertError(body(expect(post("/api/users/login", "{\"user\":{\"email\":\"" + dupEmail + "\",\"password\":\"\"}}", null),
                HttpStatus.UNPROCESSABLE_CONTENT, "login blank password")), "password", "can't be blank", "login blank password");
        assertError(body(expect(post("/api/users/login", "{\"user\":{\"email\":\"" + dupEmail + "\",\"password\":\"wrongpassword\"}}", null),
                HttpStatus.UNAUTHORIZED, "login wrong password")), "credentials", "invalid", "login wrong password");

        // --- current-user endpoints require a token ---
        assertError(body(expect(get("/api/user", null), HttpStatus.UNAUTHORIZED, "GET /user no auth")),
                "token", "is missing", "GET /user no auth");
        assertError(body(expect(put("/api/user", "{\"user\":{\"bio\":\"test\"}}", null), HttpStatus.UNAUTHORIZED, "PUT /user no auth")),
                "token", "is missing", "PUT /user no auth");

        // --- required identity fields cannot be blanked or nulled ---
        expect(put("/api/user", "{\"user\":{\"email\":\"\"}}", token), HttpStatus.UNPROCESSABLE_CONTENT, "reject email ''");
        expect(put("/api/user", "{\"user\":{\"username\":\"\"}}", token), HttpStatus.UNPROCESSABLE_CONTENT, "reject username ''");
        expect(put("/api/user", "{\"user\":{\"email\":null}}", token), HttpStatus.UNPROCESSABLE_CONTENT, "reject email null");
        expect(put("/api/user", "{\"user\":{\"username\":null}}", token), HttpStatus.UNPROCESSABLE_CONTENT, "reject username null");

        // --- password policy: >= 8 chars, accept up to 64 (NIST 800-63B) ---
        expect(put("/api/user", "{\"user\":{\"password\":\"\"}}", token), HttpStatus.UNPROCESSABLE_CONTENT, "reject password ''");
        expect(put("/api/user", "{\"user\":{\"password\":null}}", token), HttpStatus.UNPROCESSABLE_CONTENT, "reject password null");
        expect(put("/api/user", "{\"user\":{\"password\":\"short7c\"}}", token), HttpStatus.UNPROCESSABLE_CONTENT, "reject 7-char password");
        expect(put("/api/user", "{\"user\":{\"password\":\"bonjour1\"}}", token), HttpStatus.OK, "accept 8-char password");
        expect(put("/api/user", "{\"user\":{\"password\":\"" + "a".repeat(64) + "\"}}", token), HttpStatus.OK, "accept 64-char password");
    }
}
