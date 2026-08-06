package com.enoca.requestmanagement.detail;

import com.enoca.requestmanagement.dto.request.CreateRequestDto;
import com.enoca.requestmanagement.dto.response.detail.RequestDetailResponse;
import com.enoca.requestmanagement.entity.detail.RequestDetail;
import com.enoca.requestmanagement.enums.RequestType;

public interface RequestDetailHandler {

    RequestType supportedType();

    RequestDetail toEntity(CreateRequestDto dto);

    void updateEntity(RequestDetail existing, CreateRequestDto dto);

    RequestDetailResponse toResponse(RequestDetail detail);
}
