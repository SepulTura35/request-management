package com.enoca.requestmanagement.rule.impl;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.detail.ExpenseRequestDetail;
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
public class ExpenseApprovalRule implements ApprovalRuleEngine {

    private final ApprovalThresholdProperties thresholds;

    @Override
    public RequestType supportedType() {
        return RequestType.EXPENSE;
    }

    @Override
    public List<Role> determineApprovalChain(Request request) {
        BigDecimal amount = ((ExpenseRequestDetail) request.getDetail()).getAmount();

        if (amount.compareTo(thresholds.expenseFinanceThreshold()) <= 0) {
            return List.of(Role.MANAGER);
        }
        if (amount.compareTo(thresholds.expenseDirectorThreshold()) <= 0) {
            return List.of(Role.MANAGER, Role.FINANCE);
        }
        return List.of(Role.MANAGER, Role.FINANCE, Role.DIRECTOR);
    }
}
