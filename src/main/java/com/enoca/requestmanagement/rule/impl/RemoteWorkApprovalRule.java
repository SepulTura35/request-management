package com.enoca.requestmanagement.rule.impl;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.detail.RemoteWorkRequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.rule.ApprovalRuleEngine;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class RemoteWorkApprovalRule implements ApprovalRuleEngine {

    static final long HR_APPROVAL_THRESHOLD_DAYS = 5;

    @Override
    public RequestType supportedType() {
        return RequestType.REMOTE_WORK;
    }

    @Override
    public List<Role> determineApprovalChain(Request request) {
        RemoteWorkRequestDetail detail = (RemoteWorkRequestDetail) request.getDetail();
        long totalDays = ChronoUnit.DAYS.between(detail.getStartDate(), detail.getEndDate()) + 1;

        if (totalDays <= HR_APPROVAL_THRESHOLD_DAYS) {
            return List.of(Role.MANAGER);
        }
        return List.of(Role.MANAGER, Role.HR);
    }
}
