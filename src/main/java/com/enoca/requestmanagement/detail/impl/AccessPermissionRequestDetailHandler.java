package com.enoca.requestmanagement.detail.impl;

import com.enoca.requestmanagement.detail.RequestDetailHandler;
import com.enoca.requestmanagement.dto.request.CreateAccessPermissionRequestDto;
import com.enoca.requestmanagement.dto.request.CreateRequestDto;
import com.enoca.requestmanagement.dto.response.detail.AccessPermissionDetailResponse;
import com.enoca.requestmanagement.dto.response.detail.RequestDetailResponse;
import com.enoca.requestmanagement.entity.detail.AccessPermissionRequestDetail;
import com.enoca.requestmanagement.entity.detail.RequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import org.springframework.stereotype.Component;

@Component
public class AccessPermissionRequestDetailHandler implements RequestDetailHandler {

    @Override
    public RequestType supportedType() {
        return RequestType.ACCESS_PERMISSION;
    }

    @Override
    public RequestDetail toEntity(CreateRequestDto dto) {
        AccessPermissionRequestDetail detail = new AccessPermissionRequestDetail();
        apply(detail, (CreateAccessPermissionRequestDto) dto);
        return detail;
    }

    @Override
    public void updateEntity(RequestDetail existing, CreateRequestDto dto) {
        apply((AccessPermissionRequestDetail) existing, (CreateAccessPermissionRequestDto) dto);
    }

    @Override
    public RequestDetailResponse toResponse(RequestDetail detail) {
        return AccessPermissionDetailResponse.from((AccessPermissionRequestDetail) detail);
    }

    private void apply(AccessPermissionRequestDetail detail, CreateAccessPermissionRequestDto dto) {
        detail.setSystemName(dto.systemName());
        detail.setAccessLevel(dto.accessLevel());
        detail.setJustification(dto.justification());
    }
}
