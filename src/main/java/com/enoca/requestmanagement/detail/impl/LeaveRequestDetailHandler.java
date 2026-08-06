package com.enoca.requestmanagement.detail.impl;

import com.enoca.requestmanagement.detail.RequestDetailHandler;
import com.enoca.requestmanagement.dto.request.CreateLeaveRequestDto;
import com.enoca.requestmanagement.dto.request.CreateRequestDto;
import com.enoca.requestmanagement.dto.response.detail.LeaveDetailResponse;
import com.enoca.requestmanagement.dto.response.detail.RequestDetailResponse;
import com.enoca.requestmanagement.entity.detail.LeaveRequestDetail;
import com.enoca.requestmanagement.entity.detail.RequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class LeaveRequestDetailHandler implements RequestDetailHandler {

    private static final int MAX_LEAVE_DAYS = 60;

    @Override
    public RequestType supportedType() {
        return RequestType.LEAVE;
    }

    @Override
    public RequestDetail toEntity(CreateRequestDto dto) {
        LeaveRequestDetail detail = new LeaveRequestDetail();
        apply(detail, (CreateLeaveRequestDto) dto);
        return detail;
    }

    @Override
    public void updateEntity(RequestDetail existing, CreateRequestDto dto) {
        apply((LeaveRequestDetail) existing, (CreateLeaveRequestDto) dto);
    }

    @Override
    public RequestDetailResponse toResponse(RequestDetail detail) {
        return LeaveDetailResponse.from((LeaveRequestDetail) detail);
    }

    private void apply(LeaveRequestDetail detail, CreateLeaveRequestDto dto) {
        if (dto.endDate().isBefore(dto.startDate())) {
            throw new BusinessRuleException("Bitiş tarihi başlangıç tarihinden önce olamaz");
        }

        int totalDays = (int) ChronoUnit.DAYS.between(dto.startDate(), dto.endDate()) + 1;
        if (totalDays > MAX_LEAVE_DAYS) {
            throw new BusinessRuleException("İzin talebi en fazla " + MAX_LEAVE_DAYS + " gün olabilir");
        }

        detail.setLeaveType(dto.leaveType());
        detail.setStartDate(dto.startDate());
        detail.setEndDate(dto.endDate());
        detail.setTotalDays(totalDays);
    }
}
