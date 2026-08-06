package com.enoca.requestmanagement.service;

import com.enoca.requestmanagement.dto.request.ApproveRequest;
import com.enoca.requestmanagement.dto.request.RejectRequest;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.dto.response.PendingApprovalResponse;
import com.enoca.requestmanagement.dto.response.RequestResponse;
import com.enoca.requestmanagement.entity.User;
import org.springframework.data.domain.Pageable;

public interface ApprovalService {

    RequestResponse approve(Long stepId, ApproveRequest action, User approver);

    RequestResponse reject(Long stepId, RejectRequest action, User approver);

    PageResponse<PendingApprovalResponse> findPending(User approver, Pageable pageable);

    PageResponse<PendingApprovalResponse> findHistory(User approver, Pageable pageable);
}
