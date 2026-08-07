package com.enoca.requestmanagement;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Güvenlik sıkılaştırmaları")
class SecurityHardeningIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockMvc mockMvc;

    @Autowired
    SecurityHardeningIntegrationTest(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    @DisplayName("Pasifleştirilen kullanıcının mevcut token'ı reddedilir")
    void deactivatedUserTokenIsRejected() throws Exception {
        String registerBody = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Gecici","lastName":"Kullanici",
                                 "email":"hardening.deneme@enoca.com","password":"Password123",
                                 "departmentCode":"IT"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String userToken = objectMapper.readTree(registerBody).get("token").asText();
        long userId = objectMapper.readTree(registerBody).get("userId").asLong();

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        String adminToken = login("admin@enoca.com", "Password123");
        mockMvc.perform(patch("/api/admin/users/" + userId + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("active", "false"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Ard arda başarısız girişler 429 ile durdurulur")
    void repeatedFailedLoginsAreRateLimited() throws Exception {
        String email = "kaba.kuvvet@enoca.com";
        String wrongLogin = "{\"email\":\"%s\",\"password\":\"yanlis\"}".formatted(email);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(wrongLogin))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongLogin))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("Kayıt çakışması e-postanın var olduğunu sızdırmaz")
    void duplicateRegistrationDoesNotLeakExistence() throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Mert","lastName":"Demir",
                                 "email":"mert.demir@enoca.com","password":"Password123",
                                 "departmentCode":"IT"}
                                """))
                .andExpect(status().isConflict())
                .andReturn().getResponse().getContentAsString();

        assertThat(body)
                .as("cevap gövdesi hangi e-postanın kayıtlı olduğunu söylememeli")
                .doesNotContain("mert.demir@enoca.com");
    }
}
