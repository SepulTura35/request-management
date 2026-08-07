package com.enoca.requestmanagement;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("PostgreSQL uzerinde migration ve kisitlar")
class PostgresMigrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    PostgresMigrationTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @DynamicPropertySource
    static void productionLikeSettings(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.locations", () -> "classpath:db/migration,classpath:db/migration-prod");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("app.jwt.secret", () -> "dGVzdC1vbmx5LWtleS1mb3ItdGVzdGNvbnRhaW5lcnMtcG9zdGdyZXMtcnVu");
    }

    @Test
    @DisplayName("Uretim migrationlari PostgreSQL uzerinde eksiksiz uygulanir")
    void productionMigrationsApplyCleanly() {
        Integer applied = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true", Integer.class);

        assertThat(applied).as("semanin tamami uygulanmali").isGreaterThanOrEqualTo(7);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'", Integer.class))
                .isGreaterThan(8);
    }

    @Test
    @DisplayName("Uretim semasi demo kullanici icermez")
    void productionSchemaCarriesNoDemoAccounts() {
        Integer users = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

        assertThat(users)
                .as("uretim migrationlari kullanici olusturmamali")
                .isZero();
    }

    @Test
    @DisplayName("Talep numarasi sequence'i PostgreSQL uzerinde calisir")
    void requestNumberSequenceWorks() {
        Long first = jdbcTemplate.queryForObject("SELECT nextval('request_number_seq')", Long.class);
        Long second = jdbcTemplate.queryForObject("SELECT nextval('request_number_seq')", Long.class);

        assertThat(second).isGreaterThan(first);
    }

    @Test
    @DisplayName("Optimistic locking kolonlari PostgreSQL uzerinde mevcut")
    void versionColumnsExist() {
        assertThat(columnExists("requests", "version")).isTrue();
        assertThat(columnExists("approval_steps", "version")).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("CHECK kisitlari PostgreSQL uzerinde zorlanir")
    void checkConstraintsAreEnforced() {
        Long departmentId = jdbcTemplate.queryForObject("""
                INSERT INTO departments (name, code, created_at, updated_at)
                VALUES ('Kisit Testi', 'CHK', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) RETURNING id
                """, Long.class);

        Long userId = jdbcTemplate.queryForObject("""
                INSERT INTO users (first_name, last_name, email, password, role, department_id,
                                   active, created_at, updated_at)
                VALUES ('Kisit', 'Testi', 'kisit@enoca.com', 'x', 'EMPLOYEE', ?, TRUE,
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) RETURNING id
                """, Long.class, departmentId);

        Long requestId = jdbcTemplate.queryForObject("""
                INSERT INTO requests (version, request_number, request_type, requester_id, status,
                                      priority, description, created_at, updated_at)
                VALUES (0, 'CHK-1', 'EXPENSE', ?, 'DRAFT', 'MEDIUM', 'Kisit testi',
                        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) RETURNING id
                """, Long.class, userId);

        Long detailId = jdbcTemplate.queryForObject("""
                INSERT INTO request_details (detail_type, request_id, created_at, updated_at)
                VALUES ('ExpenseRequestDetail', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) RETURNING id
                """, Long.class, requestId);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO expense_request_details (id, amount, expense_category) VALUES (?, -50, 'MEALS')
                """, detailId))
                .as("negatif tutar PostgreSQL tarafindan da reddedilmeli")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM information_schema.columns
                WHERE table_name = ? AND column_name = ?
                """, Integer.class, table, column);
        return count != null && count > 0;
    }
}
