package com.enoca.requestmanagement.detail.impl;

import com.enoca.requestmanagement.detail.RequestDetailHandler;
import com.enoca.requestmanagement.dto.request.CreateRemoteWorkRequestDto;
import com.enoca.requestmanagement.dto.request.CreateRequestDto;
import com.enoca.requestmanagement.dto.response.detail.RemoteWorkDetailResponse;
import com.enoca.requestmanagement.dto.response.detail.RequestDetailResponse;
import com.enoca.requestmanagement.entity.detail.RemoteWorkRequestDetail;
import com.enoca.requestmanagement.entity.detail.RequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

@Component
public class RemoteWorkRequestDetailHandler implements RequestDetailHandler {

    @Override
    public RequestType supportedType() {
        return RequestType.REMOTE_WORK;
    }

    @Override
    public RequestDetail toEntity(CreateRequestDto dto) {
        RemoteWorkRequestDetail detail = new RemoteWorkRequestDetail();
        apply(detail, (CreateRemoteWorkRequestDto) dto);
        return detail;
    }

    @Override
    public void updateEntity(RequestDetail existing, CreateRequestDto dto) {
        apply((RemoteWorkRequestDetail) existing, (CreateRemoteWorkRequestDto) dto);
    }

    @Override
    public RequestDetailResponse toResponse(RequestDetail detail) {
        return RemoteWorkDetailResponse.from((RemoteWorkRequestDetail) detail);
    }

    private void apply(RemoteWorkRequestDetail detail, CreateRemoteWorkRequestDto dto) {
        if (dto.endDate().isBefore(dto.startDate())) {
            throw new BusinessRuleException("Bitiş tarihi başlangıç tarihinden önce olamaz");
        }

        detail.setStartDate(dto.startDate());
        detail.setEndDate(dto.endDate());
        detail.setWorkLocation(dto.workLocation());
    }
}
