package com.enoca.requestmanagement.rule;

import com.enoca.requestmanagement.entity.Request;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.rule.impl.ExpenseApprovalRule;
import com.enoca.requestmanagement.rule.impl.LeaveApprovalRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApprovalRuleEngineRegistryTest {

    private static final ApprovalThresholdProperties THRESHOLDS = new ApprovalThresholdProperties(
            3, new BigDecimal("1000"), new BigDecimal("5000"), new BigDecimal("50000"), 5);

    @Test
    void indexesEnginesBySupportedType() {
        ApprovalRuleEngineRegistry registry = new ApprovalRuleEngineRegistry(
                List.of(new LeaveApprovalRule(THRESHOLDS), new ExpenseApprovalRule(THRESHOLDS)));

        assertThat(registry.resolve(RequestType.LEAVE)).isInstanceOf(LeaveApprovalRule.class);
        assertThat(registry.resolve(RequestType.EXPENSE)).isInstanceOf(ExpenseApprovalRule.class);
    }

    @Test
    void reportsRequestTypesWithoutAnEngine() {
        ApprovalRuleEngineRegistry registry = new ApprovalRuleEngineRegistry(List.of(new LeaveApprovalRule(THRESHOLDS)));

        assertThatThrownBy(() -> registry.resolve(RequestType.EQUIPMENT))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("EQUIPMENT");
    }

    @Test
    void refusesToStartWithTwoEnginesForOneType() {
        ApprovalRuleEngine duplicate = new ApprovalRuleEngine() {
            @Override
            public RequestType supportedType() {
                return RequestType.LEAVE;
            }

            @Override
            public List<Role> determineApprovalChain(Request request) {
                return List.of(Role.DIRECTOR);
            }
        };

        assertThatThrownBy(() -> new ApprovalRuleEngineRegistry(List.of(new LeaveApprovalRule(THRESHOLDS), duplicate)))
                .as("cakisma sessizce kazanana birakilmamali")
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("LEAVE");
    }
}
