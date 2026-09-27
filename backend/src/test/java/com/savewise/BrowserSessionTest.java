package com.savewise;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/**
 * Drives the real server over HTTP with a cookie jar, the way the browser does: fetch the CSRF
 * cookie, echo it in the header, and keep the session cookie. MockMvc's {@code csrf()} helper
 * bypasses the real token repository, so this is the test that proves the SPA contract.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BrowserSessionTest {

    @LocalServerPort
    private int port;

    private CookieManager cookies;
    private HttpClient http;

    @BeforeEach
    void setUp() {
        cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        http = HttpClient.newBuilder().cookieHandler(cookies).build();
    }

    @Test
    void csrfCookieRoundTripAndNoServerSession() throws Exception {
        HttpResponse<String> csrf = send(HttpRequest.newBuilder(uri("/api/auth/csrf")).GET());
        assertThat(csrf.statusCode()).isEqualTo(200);
        assertThat(csrf.body()).contains("X-XSRF-TOKEN");
        assertThat(csrf.headers().allValues("set-cookie")).noneMatch(c -> c.startsWith("JSESSIONID"));

        String email = "browser-" + UUID.randomUUID() + "@example.com";
        HttpResponse<String> register = post("/api/auth/register",
                "{\"fullName\":\"Browser User\",\"email\":\"" + email + "\",\"password\":\"password123\"}");
        assertThat(register.statusCode()).isEqualTo(201);
        assertThat(cookie("sw_session")).isNotNull();

        // The token must stay stable across authenticated requests so parallel calls don't race.
        String before = cookie("XSRF-TOKEN").getValue();
        assertThat(send(HttpRequest.newBuilder(uri("/api/dashboard")).GET()).statusCode()).isEqualTo(200);
        HttpResponse<String> expense = post("/api/expenses",
                "{\"description\":\"Lunch\",\"category\":\"FOOD\",\"amount\":5000,\"spentOn\":\"2026-09-03\"}");
        assertThat(expense.statusCode()).isEqualTo(201);
        assertThat(cookie("XSRF-TOKEN").getValue()).isEqualTo(before);

        HttpResponse<String> forged = send(HttpRequest.newBuilder(uri("/api/expenses"))
                .header("Content-Type", "application/json")
                .header("X-XSRF-TOKEN", "not-the-token")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"description\":\"x\",\"category\":\"FOOD\",\"amount\":1,\"spentOn\":\"2026-09-03\"}")));
        assertThat(forged.statusCode()).isEqualTo(403);
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        return send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .header("X-XSRF-TOKEN", cookie("XSRF-TOKEN").getValue())
                .POST(HttpRequest.BodyPublishers.ofString(json)));
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpCookie cookie(String name) {
        return cookies.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals(name) && !c.getValue().isEmpty())
                .findFirst()
                .orElse(null);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
