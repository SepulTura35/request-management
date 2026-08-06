package com.enoca.requestmanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("Veritabani CHECK kisitlari")
class DatabaseConstraintTest {

    private static final AtomicLong NEXT_ID = new AtomicLong(900_000);

    private final JdbcTemplate jdbcTemplate;

    private long requesterId;

    @Autowired
    DatabaseConstraintTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void resolveRequester() {
        requesterId = jdbcTemplate.queryForObject("SELECT MIN(id) FROM users", Long.class);
    }

    private long insertDetailShell(String detailType) {
        long id = NEXT_ID.incrementAndGet();

        jdbcTemplate.update("""
                INSERT INTO requests (id, version, request_number, request_type, requester_id,
                                      status, priority, description, created_at, updated_at)
                VALUES (?, 0, ?, ?, ?, 'DRAFT', 'MEDIUM', 'Kisit testi', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, id, "TEST-" + id, detailTypeToRequestType(detailType), requesterId);

        jdbcTemplate.update("""
                INSERT INTO request_details (id, detail_type, request_id, created_at, updated_at)
                VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, id, detailType, id);

        return id;
    }

    private String detailTypeToRequestType(String detailType) {
        return switch (detailType) {
            case "ExpenseRequestDetail" -> "EXPENSE";
            case "EquipmentRequestDetail" -> "EQUIPMENT";
            case "LeaveRequestDetail" -> "LEAVE";
            case "RemoteWorkRequestDetail" -> "REMOTE_WORK";
            default -> throw new IllegalArgumentException(detailType);
        };
    }

    @Test
    @DisplayName("Masraf tutari sifir veya negatif olamaz")
    void expenseAmountMustBePositive() {
        long negative = insertDetailShell("ExpenseRequestDetail");
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO expense_request_details (id, amount, expense_category) VALUES (?, -1, 'MEALS')
                """, negative))
                .isInstanceOf(DataIntegrityViolationException.class);

        long zero = insertDetailShell("ExpenseRequestDetail");
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO expense_request_details (id, amount, expense_category) VALUES (?, 0, 'MEALS')
                """, zero))
                .isInstanceOf(DataIntegrityViolationException.class);

        long valid = insertDetailShell("ExpenseRequestDetail");
        assertThatCode(() -> jdbcTemplate.update("""
                INSERT INTO expense_request_details (id, amount, expense_category) VALUES (?, 250.50, 'MEALS')
                """, valid)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ekipman maliyeti ve adedi sifirdan buyuk olmali")
    void equipmentCostAndQuantityMustBePositive() {
        long badCost = insertDetailShell("EquipmentRequestDetail");
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO equipment_request_details (id, equipment_type, estimated_cost, quantity)
                VALUES (?, 'LAPTOP', 0, 1)
                """, badCost))
                .isInstanceOf(DataIntegrityViolationException.class);

        long badQuantity = insertDetailShell("EquipmentRequestDetail");
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO equipment_request_details (id, equipment_type, estimated_cost, quantity)
                VALUES (?, 'LAPTOP', 1000, 0)
                """, badQuantity))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Izin bitis tarihi baslangictan once olamaz")
    void leaveDatesMustBeOrdered() {
        long inverted = insertDetailShell("LeaveRequestDetail");
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO leave_request_details (id, leave_type, start_date, end_date, total_days)
                VALUES (?, 'ANNUAL', DATE '2026-09-10', DATE '2026-09-01', 1)
                """, inverted))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Izin gun sayisi 1 ile 60 arasinda olmali")
    void leaveTotalDaysMustBeInRange() {
        long zeroDays = insertDetailShell("LeaveRequestDetail");
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO leave_request_details (id, leave_type, start_date, end_date, total_days)
                VALUES (?, 'ANNUAL', DATE '2026-09-01', DATE '2026-09-01', 0)
                """, zeroDays))
                .isInstanceOf(DataIntegrityViolationException.class);

        long tooMany = insertDetailShell("LeaveRequestDetail");
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO leave_request_details (id, leave_type, start_date, end_date, total_days)
                VALUES (?, 'ANNUAL', DATE '2026-01-01', DATE '2026-12-31', 365)
                """, tooMany))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Uzaktan calisma bitis tarihi baslangictan once olamaz")
    void remoteWorkDatesMustBeOrdered() {
        long inverted = insertDetailShell("RemoteWorkRequestDetail");
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO remote_work_request_details (id, start_date, end_date, work_location)
                VALUES (?, DATE '2026-09-10', DATE '2026-09-01', 'Izmir')
                """, inverted))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Onay adimi sirasi sifirdan buyuk olmali")
    void approvalStepOrderMustBePositive() {
        long requestId = insertDetailShell("LeaveRequestDetail");

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO approval_steps (id, version, request_id, step_order, approver_role,
                                            status, created_at, updated_at)
                VALUES (?, 0, ?, 0, 'MANAGER', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, NEXT_ID.incrementAndGet(), requestId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
