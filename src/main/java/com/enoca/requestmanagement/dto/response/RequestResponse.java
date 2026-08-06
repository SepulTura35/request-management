package com.enoca.requestmanagement.dto.response;

import com.enoca.requestmanagement.dto.response.detail.RequestDetailResponse;
import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestStatus;
import com.enoca.requestmanagement.enums.RequestType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequestResponse(
        Long id,
        String requestNumber,
        RequestType requestType,
        RequestStatus status,
        Priority priority,
        String description,
        Long requesterId,
        String requesterName,
        String requesterDepartment,
        LocalDateTime createdAt,
        LocalDateTime submittedAt,
        LocalDateTime resolvedAt,
        RequestDetailResponse detail,
        List<ApprovalStepResponse> approvalSteps
) {
}
