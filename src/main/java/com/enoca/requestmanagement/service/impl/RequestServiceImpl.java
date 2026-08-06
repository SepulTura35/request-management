package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.detail.RequestDetailHandler;
import com.enoca.requestmanagement.detail.RequestDetailHandlerRegistry;
import com.enoca.requestmanagement.dto.request.CreateRequestDto;
import com.enoca.requestmanagement.dto.response.ApprovalStepResponse;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.dto.response.RequestResponse;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.entity.detail.RequestDetail;
import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.exception.ResourceNotFoundException;
import com.enoca.requestmanagement.repository.RequestRepository;
import com.enoca.requestmanagement.service.RequestService;
import lombok.RequiredArgsConstructor;
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

        return toResponse(requestRepository.save(request));
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

        return toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public RequestResponse findById(Long id, User viewer) {
        Request request = requestRepository.findByIdWithRequester(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Talep", id));

        if (!canView(request, viewer)) {
            throw new AccessDeniedException("Bu talebi goruntuleme yetkiniz yok");
        }

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

        request.setStatus(RequestStatus.CANCELLED);
        request.setResolvedAt(LocalDateTime.now());

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

    private boolean canView(Request request, User viewer) {
        if (request.getRequester().getId().equals(viewer.getId()) || viewer.getRole() == Role.ADMIN) {
            return true;
        }

        return request.getApprovalSteps().stream()
                .anyMatch(step -> step.getApprover() != null
                        && step.getApprover().getId().equals(viewer.getId()));
    }

    private String nextRequestNumber() {
        String prefix = "REQ-" + Year.now().getValue() + "-";
        long sequence = requestRepository.countByRequestNumberPrefix(prefix) + 1;
        return prefix + String.format("%04d", sequence);
    }

    private RequestResponse toResponse(Request request) {
        RequestDetail detail = request.getDetail();

        List<ApprovalStepResponse> steps = request.getApprovalSteps().stream()
                .map(ApprovalStepResponse::from)
                .toList();

        return new RequestResponse(
                request.getId(),
                request.getRequestNumber(),
                request.getRequestType(),
                request.getStatus(),
                request.getPriority(),
                request.getDescription(),
                request.getRequester().getId(),
                request.getRequester().getFullName(),
                request.getRequester().getDepartment().getName(),
                request.getCreatedAt(),
                request.getSubmittedAt(),
                request.getResolvedAt(),
                detail == null ? null : detailHandlerRegistry.resolve(request.getRequestType()).toResponse(detail),
                steps);
    }
}
