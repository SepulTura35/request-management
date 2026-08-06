package com.enoca.requestmanagement.rule;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.approval")
public record ApprovalThresholdProperties(

        @DefaultValue("3") int leaveHrThresholdDays,

        @DefaultValue("1000") BigDecimal expenseFinanceThreshold,

        @DefaultValue("5000") BigDecimal expenseDirectorThreshold,

        @DefaultValue("50000") BigDecimal equipmentDirectorThreshold,

        @DefaultValue("5") long remoteWorkHrThresholdDays
) {
}
