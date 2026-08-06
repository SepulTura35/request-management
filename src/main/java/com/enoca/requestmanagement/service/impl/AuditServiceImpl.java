package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.dto.response.AuditLogResponse;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.entity.AuditLog;
import com.enoca.requestmanagement.event.AuditEvent;
import com.enoca.requestmanagement.repository.AuditLogRepository;
import com.enoca.requestmanagement.repository.UserRepository;
import com.enoca.requestmanagement.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void record(AuditEvent event) {
        AuditLog log = AuditLog.builder()
                .action(event.action())
                .entityType(event.entityType())
                .entityId(event.entityId())
                .user(event.userId() == null ? null : userRepository.getReferenceById(event.userId()))
                .details(event.details())
                .build();

        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> findAll(Pageable pageable) {
        return PageResponse.from(auditLogRepository.findAllByOrderByCreatedAtDesc(pageable),
                AuditLogResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> findForEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtAsc(entityType, entityId).stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}
