package com.enoca.requestmanagement.rule.impl;

import com.enoca.requestmanagement.TestFixtures;
import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.entity.detail.RequestDetail;
import com.enoca.requestmanagement.enums.AccessLevel;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovalRuleEngineTest {

    private static final Department DEPARTMENT = TestFixtures.department(1L, "IT");
    private static final User REQUESTER = TestFixtures.user(1L, Role.EMPLOYEE, DEPARTMENT);

    private static Request requestWith(RequestType type, RequestDetail detail) {
        return TestFixtures.request(1L, type, REQUESTER, detail);
    }

    @Nested
    @DisplayName("Izin talebi")
    class Leave {

        private final LeaveApprovalRule rule = new LeaveApprovalRule();

        @Test
        void supportsLeaveType() {
            assertThat(rule.supportedType()).isEqualTo(RequestType.LEAVE);
        }

        @ParameterizedTest(name = "{0} gun -> {1} adim")
        @CsvSource({"1, 1", "3, 1", "4, 2", "30, 2"})
        void chainLengthFollowsDayThreshold(int totalDays, int expectedSteps) {
            List<Role> chain = rule.determineApprovalChain(
                    requestWith(RequestType.LEAVE, TestFixtures.leaveDetail(totalDays)));

            assertThat(chain).hasSize(expectedSteps);
        }

        @Test
        void threeDaysNeedsOnlyManager() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.LEAVE, TestFixtures.leaveDetail(3))))
                    .containsExactly(Role.MANAGER);
        }

        @Test
        void fourDaysAddsHumanResources() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.LEAVE, TestFixtures.leaveDetail(4))))
                    .containsExactly(Role.MANAGER, Role.HR);
        }
    }

    @Nested
    @DisplayName("Masraf talebi")
    class Expense {

        private final ExpenseApprovalRule rule = new ExpenseApprovalRule();

        @Test
        void atThresholdStaysWithManager() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.EXPENSE, TestFixtures.expenseDetail("1000.00"))))
                    .containsExactly(Role.MANAGER);
        }

        @Test
        void justAboveThresholdAddsFinance() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.EXPENSE, TestFixtures.expenseDetail("1000.01"))))
                    .containsExactly(Role.MANAGER, Role.FINANCE);
        }

        @Test
        void atSecondThresholdStaysWithFinance() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.EXPENSE, TestFixtures.expenseDetail("5000.00"))))
                    .containsExactly(Role.MANAGER, Role.FINANCE);
        }

        @Test
        void justAboveSecondThresholdReachesDirector() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.EXPENSE, TestFixtures.expenseDetail("5000.01"))))
                    .containsExactly(Role.MANAGER, Role.FINANCE, Role.DIRECTOR);
        }
    }

    @Nested
    @DisplayName("Ekipman talebi")
    class Equipment {

        private final EquipmentApprovalRule rule = new EquipmentApprovalRule();

        @Test
        void alwaysIncludesInformationTechnology() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.EQUIPMENT, TestFixtures.equipmentDetail("1000.00", 1))))
                    .containsExactly(Role.MANAGER, Role.IT);
        }

        @Test
        void weighsQuantityNotOnlyUnitCost() {
            Request cheapButMany = requestWith(
                    RequestType.EQUIPMENT, TestFixtures.equipmentDetail("20000.00", 5));

            assertThat(rule.determineApprovalChain(cheapButMany))
                    .as("5 x 20000 = 100000, esik asiliyor")
                    .containsExactly(Role.MANAGER, Role.IT, Role.DIRECTOR);
        }

        @Test
        void atThresholdStaysWithoutDirector() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.EQUIPMENT, TestFixtures.equipmentDetail("25000.00", 2))))
                    .containsExactly(Role.MANAGER, Role.IT);
        }
    }

    @Nested
    @DisplayName("Uzaktan calisma talebi")
    class RemoteWork {

        private final RemoteWorkApprovalRule rule = new RemoteWorkApprovalRule();

        @Test
        void fiveDaysNeedsOnlyManager() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.REMOTE_WORK, TestFixtures.remoteWorkDetail(5))))
                    .containsExactly(Role.MANAGER);
        }

        @Test
        void sixDaysAddsHumanResources() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.REMOTE_WORK, TestFixtures.remoteWorkDetail(6))))
                    .containsExactly(Role.MANAGER, Role.HR);
        }
    }

    @Nested
    @DisplayName("Erisim yetkisi talebi")
    class AccessPermission {

        private final AccessPermissionApprovalRule rule = new AccessPermissionApprovalRule();

        @ParameterizedTest
        @CsvSource({"READ", "WRITE"})
        void readAndWriteStopAtInformationTechnology(AccessLevel level) {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.ACCESS_PERMISSION, TestFixtures.accessDetail(level))))
                    .containsExactly(Role.MANAGER, Role.IT);
        }

        @Test
        void adminAccessReachesDirector() {
            assertThat(rule.determineApprovalChain(
                    requestWith(RequestType.ACCESS_PERMISSION, TestFixtures.accessDetail(AccessLevel.ADMIN))))
                    .containsExactly(Role.MANAGER, Role.IT, Role.DIRECTOR);
        }
    }
}
