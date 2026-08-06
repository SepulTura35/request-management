package com.enoca.requestmanagement.dto.response;

import com.enoca.requestmanagement.entity.AuditLog;
import com.enoca.requestmanagement.enums.AuditAction;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuditLogResponse(
        Long id,
        AuditAction action,
        String entityType,
        Long entityId,
        Long userId,
        String userName,
        String details,
        LocalDateTime createdAt
) {

    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getUser() == null ? null : log.getUser().getId(),
                log.getUser() == null ? null : log.getUser().getFullName(),
                log.getDetails(),
                log.getCreatedAt());
    }
}
