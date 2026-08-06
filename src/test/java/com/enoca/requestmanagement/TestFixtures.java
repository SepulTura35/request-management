package com.enoca.requestmanagement;

import com.enoca.requestmanagement.entity.ApprovalStep;
import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.entity.detail.AccessPermissionRequestDetail;
import com.enoca.requestmanagement.entity.detail.EquipmentRequestDetail;
import com.enoca.requestmanagement.entity.detail.ExpenseRequestDetail;
import com.enoca.requestmanagement.entity.detail.LeaveRequestDetail;
import com.enoca.requestmanagement.entity.detail.RemoteWorkRequestDetail;
import com.enoca.requestmanagement.entity.detail.RequestDetail;
import com.enoca.requestmanagement.enums.AccessLevel;
import com.enoca.requestmanagement.enums.ApprovalStepStatus;
import com.enoca.requestmanagement.enums.EquipmentType;
import com.enoca.requestmanagement.enums.ExpenseCategory;
import com.enoca.requestmanagement.enums.LeaveType;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static Department department(Long id, String code) {
        Department department = Department.builder().name(code + " Departmani").code(code).build();
        department.setId(id);
        return department;
    }

    public static User user(Long id, Role role, Department department) {
        User user = User.builder()
                .firstName("Test")
                .lastName(role.name())
                .email(role.name().toLowerCase() + id + "@enoca.com")
                .password("hashed")
                .role(role)
                .department(department)
                .active(true)
                .build();
        user.setId(id);
        return user;
    }

    public static Request request(Long id, RequestType type, User requester, RequestDetail detail) {
        Request request = Request.builder()
                .requestNumber("REQ-2026-%04d".formatted(id))
                .requestType(type)
                .requester(requester)
                .status(RequestStatus.DRAFT)
                .description("Test talebi")
                .build();
        request.setId(id);
        request.setDetail(detail);
        return request;
    }

    public static ApprovalStep step(Long id, int order, Role role, ApprovalStepStatus status, User approver) {
        ApprovalStep step = ApprovalStep.builder()
                .stepOrder(order)
                .approverRole(role)
                .status(status)
                .approver(approver)
                .build();
        step.setId(id);
        return step;
    }

    public static LeaveRequestDetail leaveDetail(int totalDays) {
        LeaveRequestDetail detail = new LeaveRequestDetail();
        detail.setLeaveType(LeaveType.ANNUAL);
        detail.setStartDate(LocalDate.of(2026, 9, 1));
        detail.setEndDate(LocalDate.of(2026, 9, 1).plusDays(totalDays - 1L));
        detail.setTotalDays(totalDays);
        return detail;
    }

    public static ExpenseRequestDetail expenseDetail(String amount) {
        ExpenseRequestDetail detail = new ExpenseRequestDetail();
        detail.setAmount(new BigDecimal(amount));
        detail.setExpenseCategory(ExpenseCategory.TRAVEL);
        return detail;
    }

    public static EquipmentRequestDetail equipmentDetail(String unitCost, int quantity) {
        EquipmentRequestDetail detail = new EquipmentRequestDetail();
        detail.setEquipmentType(EquipmentType.LAPTOP);
        detail.setEstimatedCost(new BigDecimal(unitCost));
        detail.setQuantity(quantity);
        return detail;
    }

    public static RemoteWorkRequestDetail remoteWorkDetail(int totalDays) {
        RemoteWorkRequestDetail detail = new RemoteWorkRequestDetail();
        detail.setStartDate(LocalDate.of(2026, 9, 1));
        detail.setEndDate(LocalDate.of(2026, 9, 1).plusDays(totalDays - 1L));
        detail.setWorkLocation("Izmir");
        return detail;
    }

    public static AccessPermissionRequestDetail accessDetail(AccessLevel level) {
        AccessPermissionRequestDetail detail = new AccessPermissionRequestDetail();
        detail.setSystemName("CRM");
        detail.setAccessLevel(level);
        detail.setJustification("Test gerekcesi");
        return detail;
    }
}
