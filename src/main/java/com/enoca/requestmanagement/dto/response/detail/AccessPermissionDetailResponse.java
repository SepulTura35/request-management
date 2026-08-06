package com.enoca.requestmanagement.dto.response.detail;

import com.enoca.requestmanagement.entity.detail.AccessPermissionRequestDetail;
import com.enoca.requestmanagement.enums.AccessLevel;

public record AccessPermissionDetailResponse(
        String systemName,
        AccessLevel accessLevel,
        String justification
) implements RequestDetailResponse {

    public static AccessPermissionDetailResponse from(AccessPermissionRequestDetail detail) {
        return new AccessPermissionDetailResponse(
                detail.getSystemName(),
                detail.getAccessLevel(),
                detail.getJustification());
    }
}
