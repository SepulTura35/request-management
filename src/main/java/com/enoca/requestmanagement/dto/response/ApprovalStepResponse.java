package com.enoca.requestmanagement.dto.response;

import com.enoca.requestmanagement.entity.ApprovalStep;
import com.enoca.requestmanagement.enums.ApprovalStepStatus;
import com.enoca.requestmanagement.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApprovalStepResponse(
        Long id,
        Integer stepOrder,
        Role approverRole,
        Long approverId,
        String approverName,
        ApprovalStepStatus status,
        String comment,
        LocalDateTime actionDate
) {

    public static ApprovalStepResponse from(ApprovalStep step) {
        return new ApprovalStepResponse(
                step.getId(),
                step.getStepOrder(),
                step.getApproverRole(),
                step.getApprover() == null ? null : step.getApprover().getId(),
                step.getApprover() == null ? null : step.getApprover().getFullName(),
                step.getStatus(),
                step.getComment(),
                step.getActionDate());
    }
}
