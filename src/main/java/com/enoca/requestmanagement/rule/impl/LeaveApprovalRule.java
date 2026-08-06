package com.enoca.requestmanagement.rule.impl;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.detail.LeaveRequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.rule.ApprovalRuleEngine;
import com.enoca.requestmanagement.rule.ApprovalThresholdProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LeaveApprovalRule implements ApprovalRuleEngine {

    private final ApprovalThresholdProperties thresholds;

    @Override
    public RequestType supportedType() {
        return RequestType.LEAVE;
    }

    @Override
    public List<Role> determineApprovalChain(Request request) {
        LeaveRequestDetail detail = (LeaveRequestDetail) request.getDetail();

        if (detail.getTotalDays() <= thresholds.leaveHrThresholdDays()) {
            return List.of(Role.MANAGER);
        }
        return List.of(Role.MANAGER, Role.HR);
    }
}
