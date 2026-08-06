package com.enoca.requestmanagement.dto.response.detail;

import com.enoca.requestmanagement.entity.detail.ExpenseRequestDetail;
import com.enoca.requestmanagement.enums.ExpenseCategory;

import java.math.BigDecimal;

public record ExpenseDetailResponse(
        BigDecimal amount,
        ExpenseCategory expenseCategory,
        String receiptNumber
) implements RequestDetailResponse {

    public static ExpenseDetailResponse from(ExpenseRequestDetail detail) {
        return new ExpenseDetailResponse(
                detail.getAmount(),
                detail.getExpenseCategory(),
                detail.getReceiptNumber());
    }
}
