package com.enoca.requestmanagement.event;

import com.enoca.requestmanagement.enums.AuditAction;

public record AuditEvent(
        AuditAction action,
        String entityType,
        Long entityId,
        Long userId,
        String details
) {

    public static AuditEvent of(AuditAction action, String entityType, Long entityId, Long userId, String details) {
        return new AuditEvent(action, entityType, entityId, userId, details);
    }
}
