package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.dto.request.ApproveRequest;
import com.enoca.requestmanagement.dto.request.RejectRequest;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.dto.response.PendingApprovalResponse;
import com.enoca.requestmanagement.dto.response.RequestResponse;
import com.enoca.requestmanagement.entity.ApprovalStep;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.ApprovalStepStatus;
import com.enoca.requestmanagement.enums.AuditAction;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.event.AuditEvent;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.exception.ResourceNotFoundException;
import com.enoca.requestmanagement.mapper.RequestResponseMapper;
import com.enoca.requestmanagement.repository.ApprovalStepRepository;
import com.enoca.requestmanagement.repository.RequestRepository;
import com.enoca.requestmanagement.rule.ApproverResolver;
import com.enoca.requestmanagement.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalStepRepository approvalStepRepository;
    private final RequestRepository requestRepository;
    private final ApproverResolver approverResolver;
    private final RequestResponseMapper requestResponseMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RequestResponse approve(Long stepId, ApproveRequest action, User approver) {
        ApprovalStep step = loadActionableStep(stepId, approver);
        Request request = step.getRequest();

        resolveStep(step, ApprovalStepStatus.APPROVED, action.comment());

        Optional<ApprovalStep> nextStep = findNextPendingStep(request, step.getStepOrder());
        if (nextStep.isPresent()) {
            ApprovalStep next = nextStep.get();
            next.setApprover(approverResolver.resolve(next.getApproverRole(), request));
        } else {
            request.setStatus(RequestStatus.APPROVED);
            request.setResolvedAt(LocalDateTime.now());
        }

        publish(AuditAction.APPROVAL_APPROVED, request, approver,
                step.getStepOrder() + ". adim (" + step.getApproverRole() + ") onaylandi");

        return requestResponseMapper.toResponse(request);
    }

    @Override
    @Transactional
    public RequestResponse reject(Long stepId, RejectRequest action, User approver) {
        ApprovalStep step = loadActionableStep(stepId, approver);
        Request request = step.getRequest();

        resolveStep(step, ApprovalStepStatus.REJECTED, action.comment());
        cancelRemainingSteps(request);

        request.setStatus(RequestStatus.REJECTED);
        request.setResolvedAt(LocalDateTime.now());

        publish(AuditAction.APPROVAL_REJECTED, request, approver,
                step.getStepOrder() + ". adim (" + step.getApproverRole() + ") reddedildi: " + action.comment());

        return requestResponseMapper.toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PendingApprovalResponse> findPending(User approver, Pageable pageable) {
        return PageResponse.from(
                approvalStepRepository.findByApproverAndStatus(approver, ApprovalStepStatus.PENDING, pageable),
                PendingApprovalResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PendingApprovalResponse> findHistory(User approver, Pageable pageable) {
        return PageResponse.from(
                approvalStepRepository.findResolvedByApprover(approver, pageable),
                PendingApprovalResponse::from);
    }

    private void publish(AuditAction action, Request request, User actor, String details) {
        eventPublisher.publishEvent(
                AuditEvent.of(action, "Request", request.getId(), actor.getId(), details));
    }

    private ApprovalStep loadActionableStep(Long stepId, User approver) {
        ApprovalStep step = approvalStepRepository.findByIdWithRequest(stepId)
                .orElseThrow(() -> ResourceNotFoundException.of("Onay adimi", stepId));

        requestRepository.findByIdForUpdate(step.getRequest().getId());

        if (step.getApprover() == null || !step.getApprover().getId().equals(approver.getId())) {
            throw new AccessDeniedException("Bu onay adimi size atanmamis");
        }
        if (step.getStatus() != ApprovalStepStatus.PENDING) {
            throw new BusinessRuleException("Bu onay adimi zaten sonuclandirilmis: " + step.getStatus());
        }
        if (step.getRequest().getStatus() != RequestStatus.PENDING_APPROVAL) {
            throw new BusinessRuleException(
                    "Talep onay bekleyen durumda degil: " + step.getRequest().getStatus());
        }
        if (hasEarlierPendingStep(step)) {
            throw new BusinessRuleException("Onceki onay adimlari henuz tamamlanmadi");
        }

        return step;
    }

    private boolean hasEarlierPendingStep(ApprovalStep step) {
        return step.getRequest().getApprovalSteps().stream()
                .anyMatch(other -> other.getStatus() == ApprovalStepStatus.PENDING
                        && other.getStepOrder() < step.getStepOrder());
    }

    private void resolveStep(ApprovalStep step, ApprovalStepStatus status, String comment) {
        step.setStatus(status);
        step.setComment(comment);
        step.setActionDate(LocalDateTime.now());
    }

    private Optional<ApprovalStep> findNextPendingStep(Request request, int currentStepOrder) {
        return request.getApprovalSteps().stream()
                .filter(candidate -> candidate.getStatus() == ApprovalStepStatus.PENDING)
                .filter(candidate -> candidate.getStepOrder() > currentStepOrder)
                .min(Comparator.comparing(ApprovalStep::getStepOrder));
    }

    private void cancelRemainingSteps(Request request) {
        request.getApprovalSteps().stream()
                .filter(step -> step.getStatus() == ApprovalStepStatus.PENDING)
                .forEach(step -> {
                    step.setStatus(ApprovalStepStatus.CANCELLED);
                    step.setActionDate(LocalDateTime.now());
                });
    }
}
