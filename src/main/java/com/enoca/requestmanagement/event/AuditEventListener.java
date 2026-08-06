package com.enoca.requestmanagement.event;

import com.enoca.requestmanagement.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditService auditService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onAuditEvent(AuditEvent event) {
        auditService.record(event);
    }
}
