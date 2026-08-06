package com.enoca.requestmanagement.rule.impl;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.detail.AccessPermissionRequestDetail;
import com.enoca.requestmanagement.enums.AccessLevel;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.rule.ApprovalRuleEngine;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccessPermissionApprovalRule implements ApprovalRuleEngine {

    @Override
    public RequestType supportedType() {
        return RequestType.ACCESS_PERMISSION;
    }

    @Override
    public List<Role> determineApprovalChain(Request request) {
        AccessPermissionRequestDetail detail = (AccessPermissionRequestDetail) request.getDetail();

        if (detail.getAccessLevel() == AccessLevel.ADMIN) {
            return List.of(Role.MANAGER, Role.IT, Role.DIRECTOR);
        }
        return List.of(Role.MANAGER, Role.IT);
    }
}
