package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.detail.RequestDetailHandler;
import com.enoca.requestmanagement.detail.RequestDetailHandlerRegistry;
import com.enoca.requestmanagement.dto.request.CreateRequestDto;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.dto.response.RequestResponse;
import com.enoca.requestmanagement.entity.ApprovalStep;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.ApprovalStepStatus;
import com.enoca.requestmanagement.enums.AuditAction;
import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.event.AuditEvent;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.exception.ResourceNotFoundException;
import com.enoca.requestmanagement.mapper.RequestResponseMapper;
import com.enoca.requestmanagement.repository.RequestRepository;
import com.enoca.requestmanagement.rule.ApprovalRuleEngineRegistry;
import com.enoca.requestmanagement.rule.ApproverResolver;
import com.enoca.requestmanagement.security.RequestAccessPolicy;
import com.enoca.requestmanagement.service.RequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private static final Set<RequestStatus> CANCELLABLE_STATUSES =
            Set.of(RequestStatus.DRAFT, RequestStatus.PENDING_APPROVAL);

    private final RequestRepository requestRepository;
    private final RequestDetailHandlerRegistry detailHandlerRegistry;
    private final ApprovalRuleEngineRegistry approvalRuleRegistry;
    private final ApproverResolver approverResolver;
    private final RequestResponseMapper requestResponseMapper;
    private final RequestAccessPolicy accessPolicy;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RequestResponse create(CreateRequestDto dto, User requester) {
        RequestDetailHandler handler = detailHandlerRegistry.resolve(dto.requestType());

        Request request = Request.builder()
                .requestNumber(nextRequestNumber())
                .requestType(dto.requestType())
                .requester(requester)
                .status(RequestStatus.DRAFT)
                .priority(dto.priority() == null ? Priority.MEDIUM : dto.priority())
                .description(dto.description())
                .build();

        request.setDetail(handler.toEntity(dto));

        Request saved = requestRepository.save(request);
        publish(AuditAction.REQUEST_CREATED, saved, requester, "Talep olusturuldu");

        return toResponse(saved);
    }

    @Override
    @Transactional
    public RequestResponse update(Long id, CreateRequestDto dto, User requester) {
        Request request = getOwnedRequest(id, requester);

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new BusinessRuleException("Yalnizca taslak durumundaki talepler duzenlenebilir");
        }
        if (request.getRequestType() != dto.requestType()) {
            throw new BusinessRuleException("Talep tipi degistirilemez");
        }

        RequestDetailHandler handler = detailHandlerRegistry.resolve(dto.requestType());

        request.setDescription(dto.description());
        request.setPriority(dto.priority() == null ? Priority.MEDIUM : dto.priority());
        handler.updateEntity(request.getDetail(), dto);

        publish(AuditAction.REQUEST_UPDATED, request, requester, "Taslak guncellendi");

        return toResponse(request);
    }

    @Override
    @Transactional
    public RequestResponse submit(Long id, User requester) {
        Request request = getOwnedRequest(id, requester);

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new BusinessRuleException("Yalnizca taslak durumundaki talepler gonderilebilir");
        }

        List<Role> approvalChain = approvalRuleRegistry.resolve(request.getRequestType())
                .determineApprovalChain(request);

        if (approvalChain.isEmpty()) {
            throw new BusinessRuleException("Bu talep icin onay zinciri olusturulamadi");
        }

        List<User> approvers = approvalChain.stream()
                .map(role -> approverResolver.resolve(role, request))
                .toList();

        for (int index = 0; index < approvalChain.size(); index++) {
            ApprovalStep step = ApprovalStep.builder()
                    .stepOrder(index + 1)
                    .approverRole(approvalChain.get(index))
                    .status(ApprovalStepStatus.PENDING)
                    .build();

            if (index == 0) {
                step.setApprover(approvers.get(0));
            }

            request.addApprovalStep(step);
        }

        request.setStatus(RequestStatus.PENDING_APPROVAL);
        request.setSubmittedAt(LocalDateTime.now());

        requestRepository.flush();

        publish(AuditAction.REQUEST_SUBMITTED, request, requester,
                "Onaya gonderildi, zincir: " + approvalChain);

        return toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public RequestResponse findById(Long id, User viewer) {
        Request request = requestRepository.findByIdWithRequester(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Talep", id));

        accessPolicy.ensureCanView(request, viewer);

        return toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RequestResponse> findMyRequests(User requester, RequestStatus status,
                                                        RequestType requestType, Pageable pageable) {
        Page<Request> page;
        if (status != null && requestType != null) {
            page = requestRepository.findByRequesterAndStatusAndRequestType(requester, status, requestType, pageable);
        } else if (status != null) {
            page = requestRepository.findByRequesterAndStatus(requester, status, pageable);
        } else if (requestType != null) {
            page = requestRepository.findByRequesterAndRequestType(requester, requestType, pageable);
        } else {
            page = requestRepository.findByRequester(requester, pageable);
        }

        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional
    public RequestResponse cancel(Long id, User requester) {
        Request request = getOwnedRequest(id, requester);

        if (!CANCELLABLE_STATUSES.contains(request.getStatus())) {
            throw new BusinessRuleException(
                    "Bu durumdaki bir talep iptal edilemez: " + request.getStatus());
        }

        request.getApprovalSteps().stream()
                .filter(step -> step.getStatus() == ApprovalStepStatus.PENDING)
                .forEach(step -> {
                    step.setStatus(ApprovalStepStatus.CANCELLED);
                    step.setActionDate(LocalDateTime.now());
                });

        request.setStatus(RequestStatus.CANCELLED);
        request.setResolvedAt(LocalDateTime.now());

        publish(AuditAction.REQUEST_CANCELLED, request, requester, "Talep sahibi tarafindan iptal edildi");

        return toResponse(request);
    }

    @Override
    @Transactional
    public void delete(Long id, User requester) {
        Request request = getOwnedRequest(id, requester);

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Yalnizca taslak durumundaki talepler silinebilir, gonderilmis talepler iptal edilmelidir");
        }

        publish(AuditAction.REQUEST_DELETED, request, requester,
                "Taslak silindi: " + request.getRequestNumber());

        requestRepository.delete(request);
    }

    private Request getOwnedRequest(Long id, User requester) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Talep", id));

        if (!request.getRequester().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Bu talep uzerinde islem yapma yetkiniz yok");
        }

        return request;
    }

    private void publish(AuditAction action, Request request, User actor, String details) {
        eventPublisher.publishEvent(
                AuditEvent.of(action, "Request", request.getId(), actor.getId(), details));
    }

    private String nextRequestNumber() {
        String prefix = "REQ-" + Year.now().getValue() + "-";
        long sequence = requestRepository.countByRequestNumberPrefix(prefix) + 1;
        return prefix + String.format("%04d", sequence);
    }

    private RequestResponse toResponse(Request request) {
        return requestResponseMapper.toResponse(request);
    }
}
