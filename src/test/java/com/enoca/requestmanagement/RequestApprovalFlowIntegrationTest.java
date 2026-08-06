package com.enoca.requestmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Talep yasam dongusu ucdan uca")
class RequestApprovalFlowIntegrationTest {

    private static final String PASSWORD = "Password123";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Autowired
    RequestApprovalFlowIntegrationTest(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String tokenFor(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    private JsonNode postJson(String url, String token, String payload) throws Exception {
        String body = mockMvc.perform(post(url)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body);
    }

    @Test
    @DisplayName("Bir masraf talebi olusturulup uc adimli zincirden gecerek onaylanir")
    void expenseRequestTravelsTheWholeApprovalChain() throws Exception {
        String employee = tokenFor("mert.demir@enoca.com");
        String manager = tokenFor("ahmet.yilmaz@enoca.com");
        String finance = tokenFor("kerem.dogan@enoca.com");
        String director = tokenFor("canan.yildirim@enoca.com");

        JsonNode created = postJson("/api/requests", employee, """
                {"requestType":"EXPENSE","description":"Konferans","amount":12000.00,
                 "expenseCategory":"TRAINING"}
                """);
        long requestId = created.get("id").asLong();
        assertThat(created.get("status").asText()).isEqualTo("DRAFT");

        JsonNode submitted = postJson("/api/requests/" + requestId + "/submit", employee, "");
        assertThat(submitted.get("status").asText()).isEqualTo("PENDING_APPROVAL");
        assertThat(submitted.get("approvalSteps")).hasSize(3);

        long managerStep = submitted.get("approvalSteps").get(0).get("id").asLong();
        long financeStep = submitted.get("approvalSteps").get(1).get("id").asLong();
        long directorStep = submitted.get("approvalSteps").get(2).get("id").asLong();

        assertThat(postJson("/api/approvals/" + managerStep + "/approve", manager,
                "{\"comment\":\"Uygun\"}").get("status").asText())
                .isEqualTo("PENDING_APPROVAL");

        assertThat(postJson("/api/approvals/" + financeStep + "/approve", finance,
                "{\"comment\":\"Kontrol edildi\"}").get("status").asText())
                .isEqualTo("PENDING_APPROVAL");

        JsonNode finished = postJson("/api/approvals/" + directorStep + "/approve", director,
                "{\"comment\":\"Onaylandi\"}");

        assertThat(finished.get("status").asText()).isEqualTo("APPROVED");
        assertThat(finished.get("resolvedAt").isNull()).isFalse();

        mockMvc.perform(get("/api/requests/" + requestId + "/audit")
                        .header("Authorization", "Bearer " + employee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("REQUEST_CREATED"));
    }

    @Test
    @DisplayName("Red gerekcesi gonderilmezse istek 400 doner")
    void rejectionWithoutAReasonIsRefused() throws Exception {
        String employee = tokenFor("mert.demir@enoca.com");
        String manager = tokenFor("ahmet.yilmaz@enoca.com");

        long requestId = postJson("/api/requests", employee, """
                {"requestType":"LEAVE","description":"Izin","leaveType":"ANNUAL",
                 "startDate":"2026-09-01","endDate":"2026-09-02"}
                """).get("id").asLong();

        long stepId = postJson("/api/requests/" + requestId + "/submit", employee, "")
                .get("approvalSteps").get(0).get("id").asLong();

        mockMvc.perform(post("/api/approvals/" + stepId + "/reject")
                        .header("Authorization", "Bearer " + manager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.comment").exists());
    }

    @Test
    @DisplayName("Token olmadan korumali endpoint 401 doner")
    void protectedEndpointsRequireAToken() throws Exception {
        mockMvc.perform(get("/api/requests")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Calisan yonetim endpointlerine erisemez")
    void adminEndpointsAreClosedToEmployees() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + tokenFor("mert.demir@enoca.com")))
                .andExpect(status().isForbidden());
    }
}
