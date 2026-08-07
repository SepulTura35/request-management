package com.enoca.requestmanagement.bootstrap;

import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.repository.DepartmentRepository;
import com.enoca.requestmanagement.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:bootstrapdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=validate",
                "spring.flyway.locations=classpath:db/migration",
                "app.jwt.secret=dGVzdC1vbmx5LWtleS1mb3ItYm9vdHN0cmFwLWludGVncmF0aW9uLXRlc3Q=",
                "app.bootstrap.admin-email=ilk.yonetici@enoca.com",
                "app.bootstrap.admin-password=BootstrapParola1"
        })
@DisplayName("Bos veritabaninda ilk yonetici")
class AdminBootstrapIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Demo verisi olmayan semada tek bir ADMIN olusur")
    void bootstrapCreatesExactlyOneAdmin() {
        assertThat(userRepository.count())
                .as("bootstrap yalnizca bir hesap olusturmali")
                .isEqualTo(1);

        User admin = userRepository.findByEmail("ilk.yonetici@enoca.com").orElseThrow();

        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.isActive()).isTrue();
        assertThat(departmentRepository.findByCode("MGMT")).isPresent();
    }

    @Test
    @DisplayName("Olusan hesap gercekten giris yapabiliyor")
    void bootstrappedAdminCanLogIn() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                org.springframework.http.RequestEntity
                        .post("/api/auth/login")
                        .body(Map.of("email", "ilk.yonetici@enoca.com", "password", "BootstrapParola1")),
                new org.springframework.core.ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("token");
        assertThat(response.getBody().get("role")).isEqualTo(Role.ADMIN.name());
    }

    @Test
    @DisplayName("Parola veritabaninda duz metin degil")
    void storedPasswordIsHashed() {
        User admin = userRepository.findByEmail("ilk.yonetici@enoca.com").orElseThrow();

        assertThat(admin.getPassword())
                .as("parola BCrypt ile saklanmali")
                .startsWith("$2a$")
                .isNotEqualTo("BootstrapParola1");
    }
}
