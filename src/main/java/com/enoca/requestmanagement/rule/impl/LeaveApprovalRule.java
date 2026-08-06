package com.enoca.requestmanagement.rule.impl;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.detail.LeaveRequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.rule.ApprovalRuleEngine;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaveApprovalRule implements ApprovalRuleEngine {

    static final int HR_APPROVAL_THRESHOLD_DAYS = 3;

    @Override
    public RequestType supportedType() {
        return RequestType.LEAVE;
    }

    @Override
    public List<Role> determineApprovalChain(Request request) {
        LeaveRequestDetail detail = (LeaveRequestDetail) request.getDetail();

        if (detail.getTotalDays() <= HR_APPROVAL_THRESHOLD_DAYS) {
            return List.of(Role.MANAGER);
        }
        return List.of(Role.MANAGER, Role.HR);
    }
}
