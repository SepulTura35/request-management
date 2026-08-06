package com.enoca.requestmanagement.rule.impl;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.detail.EquipmentRequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.rule.ApprovalRuleEngine;
import com.enoca.requestmanagement.rule.ApprovalThresholdProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EquipmentApprovalRule implements ApprovalRuleEngine {

    private final ApprovalThresholdProperties thresholds;

    @Override
    public RequestType supportedType() {
        return RequestType.EQUIPMENT;
    }

    @Override
    public List<Role> determineApprovalChain(Request request) {
        EquipmentRequestDetail detail = (EquipmentRequestDetail) request.getDetail();
        BigDecimal totalCost = detail.getEstimatedCost().multiply(BigDecimal.valueOf(detail.getQuantity()));

        if (totalCost.compareTo(thresholds.equipmentDirectorThreshold()) <= 0) {
            return List.of(Role.MANAGER, Role.IT);
        }
        return List.of(Role.MANAGER, Role.IT, Role.DIRECTOR);
    }
}
