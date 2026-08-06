package com.enoca.requestmanagement.detail.impl;

import com.enoca.requestmanagement.detail.RequestDetailHandlerRegistry;
import com.enoca.requestmanagement.dto.request.CreateExpenseRequestDto;
import com.enoca.requestmanagement.dto.request.CreateLeaveRequestDto;
import com.enoca.requestmanagement.dto.request.CreateRemoteWorkRequestDto;
import com.enoca.requestmanagement.entity.detail.ExpenseRequestDetail;
import com.enoca.requestmanagement.entity.detail.LeaveRequestDetail;
import com.enoca.requestmanagement.enums.ExpenseCategory;
import com.enoca.requestmanagement.enums.LeaveType;
import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestDetailHandlerTest {

    private final LeaveRequestDetailHandler leaveHandler = new LeaveRequestDetailHandler();
    private final ExpenseRequestDetailHandler expenseHandler = new ExpenseRequestDetailHandler();
    private final RemoteWorkRequestDetailHandler remoteWorkHandler = new RemoteWorkRequestDetailHandler();

    private CreateLeaveRequestDto leaveDto(LocalDate start, LocalDate end) {
        return new CreateLeaveRequestDto(RequestType.LEAVE, "Izin", Priority.MEDIUM, LeaveType.ANNUAL, start, end);
    }

    @Test
    void leaveTotalDaysCountsBothEndsOfTheRange() {
        LeaveRequestDetail detail = (LeaveRequestDetail) leaveHandler.toEntity(
                leaveDto(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 5)));

        assertThat(detail.getTotalDays()).isEqualTo(5);
    }

    @Test
    void singleDayLeaveCountsAsOneDay() {
        LeaveRequestDetail detail = (LeaveRequestDetail) leaveHandler.toEntity(
                leaveDto(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1)));

        assertThat(detail.getTotalDays()).isEqualTo(1);
    }

    @Test
    void leaveEndDateCannotPrecedeStartDate() {
        assertThatThrownBy(() -> leaveHandler.toEntity(
                leaveDto(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 1))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Bitiş tarihi");
    }

    @Test
    void leaveIsCappedAtSixtyDays() {
        assertThatThrownBy(() -> leaveHandler.toEntity(
                leaveDto(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("60");
    }

    @Test
    void remoteWorkRejectsAnInvertedDateRange() {
        assertThatThrownBy(() -> remoteWorkHandler.toEntity(new CreateRemoteWorkRequestDto(
                RequestType.REMOTE_WORK, "Uzaktan", Priority.LOW,
                LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 1), "Izmir")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void updatingMutatesTheExistingDetailInsteadOfReplacingIt() {
        CreateExpenseRequestDto original = new CreateExpenseRequestDto(
                RequestType.EXPENSE, "Masraf", Priority.LOW,
                new BigDecimal("100.00"), ExpenseCategory.MEALS, "FIS-1");
        ExpenseRequestDetail detail = (ExpenseRequestDetail) expenseHandler.toEntity(original);
        detail.setId(42L);

        expenseHandler.updateEntity(detail, new CreateExpenseRequestDto(
                RequestType.EXPENSE, "Masraf", Priority.HIGH,
                new BigDecimal("999.99"), ExpenseCategory.TRAVEL, "FIS-2"));

        assertThat(detail.getId())
                .as("ayni satir guncellenmeli, yenisi olusturulmamali")
                .isEqualTo(42L);
        assertThat(detail.getAmount()).isEqualByComparingTo("999.99");
        assertThat(detail.getExpenseCategory()).isEqualTo(ExpenseCategory.TRAVEL);
        assertThat(detail.getReceiptNumber()).isEqualTo("FIS-2");
    }

    @Test
    void registryRoutesEachTypeToItsOwnHandler() {
        RequestDetailHandlerRegistry registry = new RequestDetailHandlerRegistry(
                List.of(leaveHandler, expenseHandler, remoteWorkHandler));

        assertThat(registry.resolve(RequestType.LEAVE)).isSameAs(leaveHandler);
        assertThat(registry.resolve(RequestType.EXPENSE)).isSameAs(expenseHandler);
        assertThat(registry.resolve(RequestType.REMOTE_WORK)).isSameAs(remoteWorkHandler);
    }

    @Test
    void registryReportsAnUnhandledType() {
        RequestDetailHandlerRegistry registry = new RequestDetailHandlerRegistry(List.of(leaveHandler));

        assertThatThrownBy(() -> registry.resolve(RequestType.ACCESS_PERMISSION))
                .isInstanceOf(BusinessRuleException.class);
    }
}
