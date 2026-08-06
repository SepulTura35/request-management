package com.enoca.requestmanagement.mapper;

import com.enoca.requestmanagement.detail.RequestDetailHandlerRegistry;
import com.enoca.requestmanagement.dto.response.ApprovalStepResponse;
import com.enoca.requestmanagement.dto.response.RequestResponse;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.detail.RequestDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RequestResponseMapper {

    private final RequestDetailHandlerRegistry detailHandlerRegistry;

    public RequestResponse toResponse(Request request) {
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
