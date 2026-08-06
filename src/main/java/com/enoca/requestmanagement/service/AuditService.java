package com.enoca.requestmanagement.service;

import com.enoca.requestmanagement.dto.response.AuditLogResponse;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.event.AuditEvent;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditService {

    void record(AuditEvent event);

    PageResponse<AuditLogResponse> findAll(Pageable pageable);

    List<AuditLogResponse> findForEntity(String entityType, Long entityId);
}
