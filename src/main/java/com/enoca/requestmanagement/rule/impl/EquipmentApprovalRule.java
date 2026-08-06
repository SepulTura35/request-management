package com.enoca.requestmanagement.rule.impl;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.detail.EquipmentRequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.rule.ApprovalRuleEngine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class EquipmentApprovalRule implements ApprovalRuleEngine {

    static final BigDecimal DIRECTOR_APPROVAL_THRESHOLD = new BigDecimal("50000");

    @Override
    public RequestType supportedType() {
        return RequestType.EQUIPMENT;
    }

    @Override
    public List<Role> determineApprovalChain(Request request) {
        EquipmentRequestDetail detail = (EquipmentRequestDetail) request.getDetail();
        BigDecimal totalCost = detail.getEstimatedCost().multiply(BigDecimal.valueOf(detail.getQuantity()));

        if (totalCost.compareTo(DIRECTOR_APPROVAL_THRESHOLD) <= 0) {
            return List.of(Role.MANAGER, Role.IT);
        }
        return List.of(Role.MANAGER, Role.IT, Role.DIRECTOR);
    }
}
