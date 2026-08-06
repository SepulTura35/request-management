package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.TestFixtures;
import com.enoca.requestmanagement.detail.RequestDetailHandlerRegistry;
import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.ApprovalStepStatus;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.mapper.RequestResponseMapper;
import com.enoca.requestmanagement.repository.RequestRepository;
import com.enoca.requestmanagement.rule.ApprovalRuleEngine;
import com.enoca.requestmanagement.rule.ApprovalRuleEngineRegistry;
import com.enoca.requestmanagement.rule.ApproverResolver;
import com.enoca.requestmanagement.security.RequestAccessPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RequestDetailHandlerRegistry detailHandlerRegistry;
    @Mock
    private ApprovalRuleEngineRegistry approvalRuleRegistry;
    @Mock
    private ApproverResolver approverResolver;
    @Mock
    private RequestResponseMapper requestResponseMapper;
    @Mock
    private RequestAccessPolicy accessPolicy;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ApprovalRuleEngine leaveRule;

    @InjectMocks
    private RequestServiceImpl requestService;

    private Department it;
    private User employee;
    private User manager;
    private User hr;
    private Request draft;

    @BeforeEach
    void setUp() {
        it = TestFixtures.department(1L, "IT");
        employee = TestFixtures.user(10L, Role.EMPLOYEE, it);
        manager = TestFixtures.user(11L, Role.MANAGER, it);
        hr = TestFixtures.user(12L, Role.HR, it);
        draft = TestFixtures.request(1L, RequestType.LEAVE, employee, TestFixtures.leaveDetail(10));

        when(requestRepository.findById(1L)).thenReturn(Optional.of(draft));
    }

    @Test
    void submitBuildsOneStepPerRoleInChainOrder() {
        when(approvalRuleRegistry.resolve(RequestType.LEAVE)).thenReturn(leaveRule);
        when(leaveRule.determineApprovalChain(draft)).thenReturn(List.of(Role.MANAGER, Role.HR));
        when(approverResolver.resolve(Role.MANAGER, draft)).thenReturn(manager);
        when(approverResolver.resolve(Role.HR, draft)).thenReturn(hr);

        requestService.submit(1L, employee);

        assertThat(draft.getStatus()).isEqualTo(RequestStatus.PENDING_APPROVAL);
        assertThat(draft.getSubmittedAt()).isNotNull();
        assertThat(draft.getApprovalSteps())
                .extracting("stepOrder", "approverRole", "status")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1, Role.MANAGER, ApprovalStepStatus.PENDING),
                        org.assertj.core.groups.Tuple.tuple(2, Role.HR, ApprovalStepStatus.PENDING));
    }

    @Test
    void submitAssignsAnApproverOnlyToTheActiveStep() {
        when(approvalRuleRegistry.resolve(RequestType.LEAVE)).thenReturn(leaveRule);
        when(leaveRule.determineApprovalChain(draft)).thenReturn(List.of(Role.MANAGER, Role.HR));
        when(approverResolver.resolve(Role.MANAGER, draft)).thenReturn(manager);
        when(approverResolver.resolve(Role.HR, draft)).thenReturn(hr);

        requestService.submit(1L, employee);

        assertThat(draft.getApprovalSteps().get(0).getApprover()).isEqualTo(manager);
        assertThat(draft.getApprovalSteps().get(1).getApprover())
                .as("sonraki adim sirasi gelince atanir")
                .isNull();
    }

    @Test
    void submitResolvesWholeChainSoAnUnfillableStepFailsImmediately() {
        when(approvalRuleRegistry.resolve(RequestType.LEAVE)).thenReturn(leaveRule);
        when(leaveRule.determineApprovalChain(draft)).thenReturn(List.of(Role.MANAGER, Role.HR));
        when(approverResolver.resolve(Role.MANAGER, draft)).thenReturn(manager);
        when(approverResolver.resolve(Role.HR, draft))
                .thenThrow(new BusinessRuleException("Bu adim icin uygun bir onayci bulunamadi: HR"));

        assertThatThrownBy(() -> requestService.submit(1L, employee))
                .isInstanceOf(BusinessRuleException.class);

        assertThat(draft.getStatus())
                .as("zincir kurulamadiysa talep taslak kalmali")
                .isEqualTo(RequestStatus.DRAFT);
    }

    @Test
    void alreadySubmittedRequestCannotBeSubmittedAgain() {
        draft.setStatus(RequestStatus.PENDING_APPROVAL);

        assertThatThrownBy(() -> requestService.submit(1L, employee))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("taslak");

        verify(approvalRuleRegistry, never()).resolve(any());
    }

    @Test
    void anotherUserCannotSubmitSomeoneElsesDraft() {
        User outsider = TestFixtures.user(99L, Role.EMPLOYEE, it);

        assertThatThrownBy(() -> requestService.submit(1L, outsider))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void cancellingClosesStepsThatWereStillWaiting() {
        draft.setStatus(RequestStatus.PENDING_APPROVAL);
        draft.addApprovalStep(TestFixtures.step(1L, 1, Role.MANAGER, ApprovalStepStatus.APPROVED, manager));
        draft.addApprovalStep(TestFixtures.step(2L, 2, Role.HR, ApprovalStepStatus.PENDING, hr));

        requestService.cancel(1L, employee);

        assertThat(draft.getStatus()).isEqualTo(RequestStatus.CANCELLED);
        assertThat(draft.getResolvedAt()).isNotNull();
        assertThat(draft.getApprovalSteps())
                .extracting("status")
                .as("bekleyen adim onaycinin kuyrugunda kalmamali")
                .containsExactly(ApprovalStepStatus.APPROVED, ApprovalStepStatus.CANCELLED);
    }

    @Test
    void submittedRequestCannotBeDeleted() {
        draft.setStatus(RequestStatus.PENDING_APPROVAL);

        assertThatThrownBy(() -> requestService.delete(1L, employee))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("iptal");

        verify(requestRepository, never()).delete(any(Request.class));
    }

    @Test
    void draftCanBeDeleted() {
        requestService.delete(1L, employee);

        verify(requestRepository).delete(draft);
    }
}
