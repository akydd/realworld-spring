package com.akydd.realworld_spring.api;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared harness for the API tests that mirror {@code ../realworld/specs/api/hurl}. Each subclass
 * matches one {@code .hurl} file.
 *
 * <p>Bodies are sent and read as <b>raw JSON</b> — never via the app's ObjectMapper, whose global
 * wrap/unwrap-root-value settings would mangle client-side payloads. Every assertion carries a
 * {@code .as(...)} description so a failure reads clearly in IntelliJ's test runner.
 *
 * <p>All suites share one Postgres container and one application context (Spring context caching),
 * so the container starts once for the whole run.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Tag("integration")
abstract class ApiTestSupport {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:18"));

    private static final AtomicInteger COUNTER = new AtomicInteger();

    @Autowired
    protected TestRestTemplate rest;

    /** Plain mapper for wire JSON — deliberately not the app's wrap/unwrap-configured ObjectMapper. */
    protected final JsonMapper json = JsonMapper.builder().build();

    /** A unique-per-call suffix, mirroring hurl's {@code {{uid}}}. */
    protected String uid() {
        return System.nanoTime() + "x" + COUNTER.incrementAndGet();
    }

    // --- HTTP (raw JSON in / raw JSON out) ---

    protected ResponseEntity<String> post(String path, String body, String token) {
        return call(HttpMethod.POST, path, body, token);
    }

    protected ResponseEntity<String> put(String path, String body, String token) {
        return call(HttpMethod.PUT, path, body, token);
    }

    protected ResponseEntity<String> get(String path, String token) {
        return call(HttpMethod.GET, path, null, token);
    }

    protected ResponseEntity<String> delete(String path, String token) {
        return call(HttpMethod.DELETE, path, null, token);
    }

    private ResponseEntity<String> call(HttpMethod method, String path, String body, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        if (token != null) {
            headers.set(HttpHeaders.AUTHORIZATION, "Token " + token);
        }
        return rest.exchange(path, method, new HttpEntity<>(body, headers), String.class);
    }

    // --- JSON ---

    protected JsonNode body(ResponseEntity<String> response) {
        return json.readTree(response.getBody());
    }

    protected List<String> strings(JsonNode arrayNode) {
        List<String> out = new ArrayList<>();
        arrayNode.forEach(n -> out.add(n.asString()));
        return out;
    }

    // --- common setup ---

    /** Register a user (asserting 201) and return their token. */
    protected String register(String username, String email, String password) {
        ResponseEntity<String> response = post("/api/users", """
                {"user":{"username":"%s","email":"%s","password":"%s"}}"""
                .formatted(username, email, password), null);
        assertThat(response.getStatusCode())
                .as("setup: register '%s' should return 201 (body: %s)", username, response.getBody())
                .isEqualTo(HttpStatus.CREATED);
        return body(response).path("user").path("token").asString();
    }

    /** Matches the leading ISO-8601 date-time the API emits, e.g. {@code 2026-08-23T21:09:52...}. */
    protected static final String ISO_TS = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*";

    /**
     * Assert a response's status (by numeric code, so it is immune to enum aliases like
     * UNPROCESSABLE_ENTITY vs UNPROCESSABLE_CONTENT), and return it for further reads.
     */
    protected ResponseEntity<String> expect(ResponseEntity<String> response, HttpStatus status, String step) {
        assertThat(response.getStatusCode().value())
                .as("%s: expected %s (body: %s)", step, status, response.getBody())
                .isEqualTo(status.value());
        return response;
    }

    /** Create an article (asserting 201) and return its slug. */
    protected String createArticle(String token, String title, String description, String articleBody) {
        ResponseEntity<String> response = expect(post("/api/articles", """
                {"article":{"title":"%s","description":"%s","body":"%s"}}"""
                .formatted(title, description, articleBody), token),
                HttpStatus.CREATED, "setup: create article '" + title + "'");
        return body(response).path("article").path("slug").asString();
    }

    /** Assert the RealWorld error shape {@code {"errors":{field:[message]}}}. */
    protected void assertError(JsonNode responseBody, String field, String message, String step) {
        assertThat(responseBody.path("errors").path(field).path(0).asString())
                .as("%s: errors.%s[0] should be '%s'", step, field, message)
                .isEqualTo(message);
    }
}
