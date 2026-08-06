package com.enoca.requestmanagement.dto.response.detail;

import com.enoca.requestmanagement.entity.detail.LeaveRequestDetail;
import com.enoca.requestmanagement.enums.LeaveType;

import java.time.LocalDate;

public record LeaveDetailResponse(
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        Integer totalDays
) implements RequestDetailResponse {

    public static LeaveDetailResponse from(LeaveRequestDetail detail) {
        return new LeaveDetailResponse(
                detail.getLeaveType(),
                detail.getStartDate(),
                detail.getEndDate(),
                detail.getTotalDays());
    }
}
