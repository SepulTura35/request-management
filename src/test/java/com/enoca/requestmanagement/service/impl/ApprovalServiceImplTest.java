package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.TestFixtures;
import com.enoca.requestmanagement.dto.request.ApproveRequest;
import com.enoca.requestmanagement.dto.request.RejectRequest;
import com.enoca.requestmanagement.entity.ApprovalStep;
import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.ApprovalStepStatus;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.mapper.RequestResponseMapper;
import com.enoca.requestmanagement.repository.ApprovalStepRepository;
import com.enoca.requestmanagement.repository.RequestRepository;
import com.enoca.requestmanagement.rule.ApproverResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApprovalServiceImplTest {

    @Mock
    private ApprovalStepRepository approvalStepRepository;
    @Mock
    private RequestRepository requestRepository;
    @Mock
    private ApproverResolver approverResolver;
    @Mock
    private RequestResponseMapper requestResponseMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private User employee;
    private User manager;
    private User finance;
    private User director;
    private Request request;
    private ApprovalStep managerStep;
    private ApprovalStep financeStep;
    private ApprovalStep directorStep;

    @BeforeEach
    void setUp() {
        Department it = TestFixtures.department(1L, "IT");
        employee = TestFixtures.user(10L, Role.EMPLOYEE, it);
        manager = TestFixtures.user(11L, Role.MANAGER, it);
        finance = TestFixtures.user(12L, Role.FINANCE, it);
        director = TestFixtures.user(13L, Role.DIRECTOR, it);

        request = TestFixtures.request(1L, RequestType.EXPENSE, employee, TestFixtures.expenseDetail("12000.00"));
        request.setStatus(RequestStatus.PENDING_APPROVAL);

        managerStep = TestFixtures.step(1L, 1, Role.MANAGER, ApprovalStepStatus.PENDING, manager);
        financeStep = TestFixtures.step(2L, 2, Role.FINANCE, ApprovalStepStatus.PENDING, null);
        directorStep = TestFixtures.step(3L, 3, Role.DIRECTOR, ApprovalStepStatus.PENDING, null);

        request.addApprovalStep(managerStep);
        request.addApprovalStep(financeStep);
        request.addApprovalStep(directorStep);

        when(approvalStepRepository.findByIdWithRequest(1L)).thenReturn(Optional.of(managerStep));
        when(approvalStepRepository.findByIdWithRequest(2L)).thenReturn(Optional.of(financeStep));
        when(requestRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(request));
    }

    @Test
    void approvingHandsTheRequestToTheNextStep() {
        when(approverResolver.resolve(Role.FINANCE, request)).thenReturn(finance);

        approvalService.approve(1L, new ApproveRequest("Butce uygun"), manager);

        assertThat(managerStep.getStatus()).isEqualTo(ApprovalStepStatus.APPROVED);
        assertThat(managerStep.getComment()).isEqualTo("Butce uygun");
        assertThat(managerStep.getActionDate()).isNotNull();
        assertThat(financeStep.getApprover())
                .as("sonraki adimin onaycisi ancak simdi belli olur")
                .isEqualTo(finance);
        assertThat(request.getStatus())
                .as("zincir bitmedi")
                .isEqualTo(RequestStatus.PENDING_APPROVAL);
    }

    @Test
    void approvingTheLastStepResolvesTheRequest() {
        managerStep.setStatus(ApprovalStepStatus.APPROVED);
        financeStep.setStatus(ApprovalStepStatus.APPROVED);
        directorStep.setApprover(director);
        when(approvalStepRepository.findByIdWithRequest(3L)).thenReturn(Optional.of(directorStep));

        approvalService.approve(3L, new ApproveRequest(null), director);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.APPROVED);
        assertThat(request.getResolvedAt()).isNotNull();
    }

    @Test
    void rejectingEndsTheRequestAndClosesUnreachedSteps() {
        approvalService.reject(1L, new RejectRequest("Butce yetersiz"), manager);

        assertThat(request.getStatus()).isEqualTo(RequestStatus.REJECTED);
        assertThat(request.getResolvedAt()).isNotNull();
        assertThat(managerStep.getComment()).isEqualTo("Butce yetersiz");
        assertThat(request.getApprovalSteps())
                .extracting("status")
                .containsExactly(
                        ApprovalStepStatus.REJECTED,
                        ApprovalStepStatus.CANCELLED,
                        ApprovalStepStatus.CANCELLED);
    }

    @Test
    void onlyTheAssignedApproverCanActOnAStep() {
        assertThatThrownBy(() -> approvalService.approve(1L, new ApproveRequest(null), finance))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void anUnassignedStepCannotBeActedOn() {
        assertThatThrownBy(() -> approvalService.approve(2L, new ApproveRequest(null), finance))
                .as("sirasi gelmemis adimin onaycisi henuz yok")
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void aResolvedStepCannotBeActedOnTwice() {
        managerStep.setStatus(ApprovalStepStatus.APPROVED);

        assertThatThrownBy(() -> approvalService.approve(1L, new ApproveRequest(null), manager))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("sonuçlandırılmış");
    }

    @Test
    void stepsOfACancelledRequestCannotBeActedOn() {
        request.setStatus(RequestStatus.CANCELLED);

        assertThatThrownBy(() -> approvalService.approve(1L, new ApproveRequest(null), manager))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("onay bekleyen durumda değil");
    }

    @Test
    void takesAWriteLockOnTheRequestBeforeActing() {
        when(approverResolver.resolve(Role.FINANCE, request)).thenReturn(finance);

        approvalService.approve(1L, new ApproveRequest(null), manager);

        org.mockito.Mockito.verify(requestRepository).findByIdForUpdate(1L);
    }
}
