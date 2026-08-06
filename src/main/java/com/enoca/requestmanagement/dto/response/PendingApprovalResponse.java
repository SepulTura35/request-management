package com.enoca.requestmanagement.dto.response;

import com.enoca.requestmanagement.entity.ApprovalStep;
import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.enums.ApprovalStepStatus;
import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PendingApprovalResponse(
        Long stepId,
        Integer stepOrder,
        Role approverRole,
        ApprovalStepStatus stepStatus,
        String comment,
        LocalDateTime actionDate,
        Long requestId,
        String requestNumber,
        RequestType requestType,
        RequestStatus requestStatus,
        Priority priority,
        String description,
        String requesterName,
        LocalDateTime submittedAt
) {

    public static PendingApprovalResponse from(ApprovalStep step) {
        Request request = step.getRequest();

        return new PendingApprovalResponse(
                step.getId(),
                step.getStepOrder(),
                step.getApproverRole(),
                step.getStatus(),
                step.getComment(),
                step.getActionDate(),
                request.getId(),
                request.getRequestNumber(),
                request.getRequestType(),
                request.getStatus(),
                request.getPriority(),
                request.getDescription(),
                request.getRequester().getFullName(),
                request.getSubmittedAt());
    }
}
