package com.enoca.requestmanagement.dto.response.detail;

import com.enoca.requestmanagement.entity.detail.RemoteWorkRequestDetail;

import java.time.LocalDate;

public record RemoteWorkDetailResponse(
        LocalDate startDate,
        LocalDate endDate,
        String workLocation
) implements RequestDetailResponse {

    public static RemoteWorkDetailResponse from(RemoteWorkRequestDetail detail) {
        return new RemoteWorkDetailResponse(
                detail.getStartDate(),
                detail.getEndDate(),
                detail.getWorkLocation());
    }
}
