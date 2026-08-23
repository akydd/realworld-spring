package com.akydd.realworld_spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * In-JVM end-to-end flow (register -> login -> create article) over real HTTP against the full
 * application, backed by a throwaway Postgres via Testcontainers. The in-process companion to the
 * external Hurl suite.
 *
 * <p>Bodies are sent and read as <b>raw JSON</b> on purpose: TestRestTemplate would otherwise use
 * the application's ObjectMapper, whose global wrap/unwrap-root-value settings mangle client-side
 * payloads. A black-box HTTP test should treat wrapping as a server concern.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ArticleFlowIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer(org.testcontainers.utility.DockerImageName.parse("postgres:18"));

    @Autowired
    TestRestTemplate rest;

    // Plain mapper for reading wire JSON — deliberately NOT the app's wrap-configured ObjectMapper.
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void register_login_thenCreateArticle() {
        // --- register a new user ---
        ResponseEntity<String> register = postJson("/api/users", """
                {"user":{"username":"alice","email":"alice@example.com","password":"password"}}""", null);
        assertThat(register.getStatusCode())
                .as("register response body: %s", register.getBody())
                .isEqualTo(HttpStatus.CREATED);

        String token = user(register).get("token").toString();
        assertThat(token).isNotBlank();

        // --- log in with the same credentials ---
        ResponseEntity<String> login = postJson("/api/users/login", """
                {"user":{"email":"alice@example.com","password":"password"}}""", null);
        assertThat(login.getStatusCode())
                .as("login response body: %s", login.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(user(login).get("token")).isNotNull();

        // --- create an article as the authenticated user ---
        ResponseEntity<String> created = postJson("/api/articles", """
                {"article":{"title":"How to Train Your Dragon","description":"Ever wonder how?","body":"You have to believe.","tagList":["dragons","training"]}}""", token);
        assertThat(created.getStatusCode())
                .as("create-article response body: %s", created.getBody())
                .isEqualTo(HttpStatus.CREATED);

        Map<String, Object> article = node(created, "article");
        assertThat(article.get("title")).isEqualTo("How to Train Your Dragon");
        assertThat(article.get("slug").toString()).isNotBlank();

        @SuppressWarnings("unchecked")
        Map<String, Object> author = (Map<String, Object>) article.get("author");
        assertThat(author).containsEntry("username", "alice");

        @SuppressWarnings("unchecked")
        List<String> tagList = (List<String>) article.get("tagList");
        assertThat(tagList).containsExactlyInAnyOrder("dragons", "training");
    }

    // --- helpers ---

    private ResponseEntity<String> postJson(String path, String rawJson, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.set(HttpHeaders.AUTHORIZATION, "Token " + token);
        }
        return rest.postForEntity(path, new HttpEntity<>(rawJson, headers), String.class);
    }

    private Map<String, Object> user(ResponseEntity<String> response) {
        return node(response, "user");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> node(ResponseEntity<String> response, String root) {
        Map<String, Object> body = json.readValue(response.getBody(), Map.class);
        return (Map<String, Object>) body.get(root);
    }
}
