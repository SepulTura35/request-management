package com.enoca.requestmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
@DisplayName("Hata cevaplari")
class ErrorResponseIntegrationTest {

    private static final String[] INTERNAL_MARKERS = {
            "com.enoca", "com.fasterxml", "org.springframework", "java.lang", "JSON parse error"
    };

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockMvc mockMvc;

    @Autowired
    ErrorResponseIntegrationTest(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private String employeeToken() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"mert.demir@enoca.com","password":"Password123"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(body).get("token").asText();
    }

    private void assertCarriesNoInternals(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        assertThat(body).as("hata govdesi ic detay tasimamali").doesNotContain(INTERNAL_MARKERS);
    }

    @Test
    @DisplayName("Bozuk JSON 400 doner ve parser detayi sizdirmaz")
    void malformedJsonIsRejectedWithoutParserDetail() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + employeeToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestType\":\"LEAVE\", bozuk}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertCarriesNoInternals(result);
    }

    @Test
    @DisplayName("Gecersiz enum degeri alan adiyla birlikte 400 doner")
    void invalidEnumValueNamesTheField() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + employeeToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requestType":"LEAVE","description":"Test","leaveType":"YOKBOYLE",
                                 "startDate":"2026-09-01","endDate":"2026-09-02"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.leaveType").exists())
                .andReturn();

        assertCarriesNoInternals(result);
        assertThat(result.getResponse().getContentAsString())
                .as("gecerli degerler istemciye soylenmeli")
                .contains("ANNUAL");
    }

    @Test
    @DisplayName("Bilinmeyen talep tipi 400 doner ve gecerli tipleri listeler")
    void unknownRequestTypeListsValidTypes() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + employeeToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestType\":\"YOKBOYLE\",\"description\":\"Test\"}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertCarriesNoInternals(result);
        assertThat(result.getResponse().getContentAsString()).contains("LEAVE", "EXPENSE");
    }

    @Test
    @DisplayName("Gecersiz enum parametresi 400 doner")
    void invalidEnumQueryParameterIsRejected() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/requests")
                        .header("Authorization", "Bearer " + employeeToken())
                        .param("status", "YOKBOYLE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.status").exists())
                .andReturn();

        assertCarriesNoInternals(result);
    }

    @Test
    @DisplayName("Desteklenmeyen HTTP metodu 405 doner")
    void unsupportedMethodIsRejected() throws Exception {
        MvcResult result = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/auth/login")
                                .header("Authorization", "Bearer " + employeeToken()))
                .andExpect(status().isMethodNotAllowed())
                .andReturn();

        assertCarriesNoInternals(result);
    }

    @Test
    @DisplayName("Bos govde 400 doner")
    void emptyBodyIsRejected() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + employeeToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertCarriesNoInternals(result);
    }

    @Test
    @DisplayName("Ayni e-posta ile ikinci kayit 409 doner")
    void duplicateEmailIsRejectedWithConflict() throws Exception {
        String payload = """
                {"firstName":"Cakisma","lastName":"Testi","email":"cakisma.testi@enoca.com",
                 "password":"Password123","departmentCode":"IT"}
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict())
                .andReturn();

        assertCarriesNoInternals(result);
    }
}
