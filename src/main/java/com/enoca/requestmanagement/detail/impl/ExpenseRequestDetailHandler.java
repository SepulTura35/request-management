package com.enoca.requestmanagement.detail.impl;

import com.enoca.requestmanagement.detail.RequestDetailHandler;
import com.enoca.requestmanagement.dto.request.CreateExpenseRequestDto;
import com.enoca.requestmanagement.dto.request.CreateRequestDto;
import com.enoca.requestmanagement.dto.response.detail.ExpenseDetailResponse;
import com.enoca.requestmanagement.dto.response.detail.RequestDetailResponse;
import com.enoca.requestmanagement.entity.detail.ExpenseRequestDetail;
import com.enoca.requestmanagement.entity.detail.RequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import org.springframework.stereotype.Component;

@Component
public class ExpenseRequestDetailHandler implements RequestDetailHandler {

    @Override
    public RequestType supportedType() {
        return RequestType.EXPENSE;
    }

    @Override
    public RequestDetail toEntity(CreateRequestDto dto) {
        ExpenseRequestDetail detail = new ExpenseRequestDetail();
        apply(detail, (CreateExpenseRequestDto) dto);
        return detail;
    }

    @Override
    public void updateEntity(RequestDetail existing, CreateRequestDto dto) {
        apply((ExpenseRequestDetail) existing, (CreateExpenseRequestDto) dto);
    }

    @Override
    public RequestDetailResponse toResponse(RequestDetail detail) {
        return ExpenseDetailResponse.from((ExpenseRequestDetail) detail);
    }

    private void apply(ExpenseRequestDetail detail, CreateExpenseRequestDto dto) {
        detail.setAmount(dto.amount());
        detail.setExpenseCategory(dto.expenseCategory());
        detail.setReceiptNumber(dto.receiptNumber());
    }
}
