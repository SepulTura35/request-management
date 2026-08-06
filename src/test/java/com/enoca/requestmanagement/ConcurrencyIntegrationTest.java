package com.enoca.requestmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DisplayName("Eszamanli istekler")
class ConcurrencyIntegrationTest {

    private static final int PARALLEL_CALLS = 8;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestRestTemplate restTemplate;
    private final int port;

    @Autowired
    ConcurrencyIntegrationTest(TestRestTemplate restTemplate, @LocalServerPort int port) {
        this.restTemplate = restTemplate;
        this.port = port;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpHeaders headers(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    private String tokenFor(String email) throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(url("/api/auth/login"), HttpMethod.POST,
                new HttpEntity<>("""
                        {"email":"%s","password":"Password123"}
                        """.formatted(email), headers(null)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return objectMapper.readTree(response.getBody()).get("token").asText();
    }

    private JsonNode post(String path, String token, String body) throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(url(path), HttpMethod.POST,
                new HttpEntity<>(body, headers(token)), String.class);
        return objectMapper.readTree(response.getBody());
    }

    private List<HttpStatus> fireInParallel(String path, String token, String body) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(PARALLEL_CALLS);
        CountDownLatch release = new CountDownLatch(1);
        List<HttpStatus> statuses = new ArrayList<>();

        List<java.util.concurrent.Future<HttpStatus>> futures = new ArrayList<>();
        for (int i = 0; i < PARALLEL_CALLS; i++) {
            futures.add(pool.submit(() -> {
                release.await();
                return HttpStatus.valueOf(restTemplate.exchange(url(path), HttpMethod.POST,
                        new HttpEntity<>(body, headers(token)), String.class).getStatusCode().value());
            }));
        }

        release.countDown();
        for (var future : futures) {
            statuses.add(future.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();

        return statuses;
    }

    @Test
    @DisplayName("Ayni taslaga paralel gonderme: biri gecer, digerleri 409 alir")
    void parallelSubmitCreatesExactlyOneChain() throws Exception {
        String employee = tokenFor("mert.demir@enoca.com");

        long requestId = post("/api/requests", employee, """
                {"requestType":"EXPENSE","description":"Paralel gonderme",
                 "amount":12000,"expenseCategory":"TRAINING"}
                """).get("id").asLong();

        List<HttpStatus> statuses = fireInParallel("/api/requests/" + requestId + "/submit", employee, "{}");

        assertThat(statuses).filteredOn(HttpStatus.OK::equals).hasSize(1);
        assertThat(statuses).filteredOn(status -> status != HttpStatus.OK)
                .as("kaybeden istekler kontrolsuz 500 degil 409 almali")
                .allMatch(HttpStatus.CONFLICT::equals);

        JsonNode detail = objectMapper.readTree(restTemplate.exchange(url("/api/requests/" + requestId),
                HttpMethod.GET, new HttpEntity<>(headers(employee)), String.class).getBody());

        assertThat(detail.get("approvalSteps"))
                .as("tek bir zincir olusmali, her istek icin ayri zincir degil")
                .hasSize(3);
        assertThat(detail.get("status").asText()).isEqualTo("PENDING_APPROVAL");
    }

    @Test
    @DisplayName("Ayni adima paralel onay: biri gecer, digerleri 409 alir")
    void parallelApproveResolvesStepOnce() throws Exception {
        String employee = tokenFor("mert.demir@enoca.com");
        String manager = tokenFor("ahmet.yilmaz@enoca.com");

        long requestId = post("/api/requests", employee, """
                {"requestType":"EXPENSE","description":"Paralel onay",
                 "amount":12000,"expenseCategory":"TRAINING"}
                """).get("id").asLong();

        long stepId = post("/api/requests/" + requestId + "/submit", employee, "{}")
                .get("approvalSteps").get(0).get("id").asLong();

        List<HttpStatus> statuses = fireInParallel("/api/approvals/" + stepId + "/approve", manager,
                "{\"comment\":\"Paralel\"}");

        assertThat(statuses).filteredOn(HttpStatus.OK::equals).hasSize(1);
        assertThat(statuses).filteredOn(status -> status != HttpStatus.OK)
                .allMatch(HttpStatus.CONFLICT::equals);

        JsonNode detail = objectMapper.readTree(restTemplate.exchange(url("/api/requests/" + requestId),
                HttpMethod.GET, new HttpEntity<>(headers(employee)), String.class).getBody());

        assertThat(detail.get("approvalSteps").get(0).get("status").asText()).isEqualTo("APPROVED");
        assertThat(detail.get("approvalSteps").get(1).get("approverName").asText())
                .as("zincir yalnizca bir adim ilerlemeli")
                .isNotBlank();
    }

    @Test
    @DisplayName("Ayni talebe paralel iptal: biri gecer, digerleri 409 alir")
    void parallelCancelResolvesRequestOnce() throws Exception {
        String employee = tokenFor("mert.demir@enoca.com");

        long requestId = post("/api/requests", employee, """
                {"requestType":"LEAVE","description":"Paralel iptal","leaveType":"ANNUAL",
                 "startDate":"2026-10-01","endDate":"2026-10-02"}
                """).get("id").asLong();

        post("/api/requests/" + requestId + "/submit", employee, "{}");

        List<HttpStatus> statuses = fireInParallel("/api/requests/" + requestId + "/cancel", employee, "{}");

        assertThat(statuses).filteredOn(HttpStatus.OK::equals).hasSize(1);
        assertThat(statuses).filteredOn(status -> status != HttpStatus.OK)
                .allMatch(HttpStatus.CONFLICT::equals);
    }

    @Test
    @DisplayName("Paralel talep olusturma benzersiz numaralar uretir")
    void parallelCreationProducesDistinctRequestNumbers() throws Exception {
        String employee = tokenFor("mert.demir@enoca.com");
        int callCount = 20;

        ExecutorService pool = Executors.newFixedThreadPool(callCount);
        CountDownLatch release = new CountDownLatch(1);
        List<java.util.concurrent.Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < callCount; i++) {
            futures.add(pool.submit(() -> {
                release.await();
                ResponseEntity<String> response = restTemplate.exchange(url("/api/requests"), HttpMethod.POST,
                        new HttpEntity<>("""
                                {"requestType":"LEAVE","description":"Paralel olusturma","leaveType":"ANNUAL",
                                 "startDate":"2026-09-01","endDate":"2026-09-02"}
                                """, headers(employee)), String.class);
                return objectMapper.readTree(response.getBody()).get("requestNumber").asText();
            }));
        }

        release.countDown();
        List<String> numbers = new ArrayList<>();
        for (var future : futures) {
            numbers.add(future.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertThat(numbers).hasSize(callCount);
        assertThat(numbers).doesNotHaveDuplicates();
    }
}
