package com.enoca.requestmanagement.rule.impl;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.entity.detail.ExpenseRequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.rule.ApprovalRuleEngine;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ExpenseApprovalRule implements ApprovalRuleEngine {

    static final BigDecimal FINANCE_APPROVAL_THRESHOLD = new BigDecimal("1000");
    static final BigDecimal DIRECTOR_APPROVAL_THRESHOLD = new BigDecimal("5000");

    @Override
    public RequestType supportedType() {
        return RequestType.EXPENSE;
    }

    @Override
    public List<Role> determineApprovalChain(Request request) {
        BigDecimal amount = ((ExpenseRequestDetail) request.getDetail()).getAmount();

        if (amount.compareTo(FINANCE_APPROVAL_THRESHOLD) <= 0) {
            return List.of(Role.MANAGER);
        }
        if (amount.compareTo(DIRECTOR_APPROVAL_THRESHOLD) <= 0) {
            return List.of(Role.MANAGER, Role.FINANCE);
        }
        return List.of(Role.MANAGER, Role.FINANCE, Role.DIRECTOR);
    }
}
