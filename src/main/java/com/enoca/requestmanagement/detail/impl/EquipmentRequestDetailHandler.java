package com.enoca.requestmanagement.detail.impl;

import com.enoca.requestmanagement.detail.RequestDetailHandler;
import com.enoca.requestmanagement.dto.request.CreateEquipmentRequestDto;
import com.enoca.requestmanagement.dto.request.CreateRequestDto;
import com.enoca.requestmanagement.dto.response.detail.EquipmentDetailResponse;
import com.enoca.requestmanagement.dto.response.detail.RequestDetailResponse;
import com.enoca.requestmanagement.entity.detail.EquipmentRequestDetail;
import com.enoca.requestmanagement.entity.detail.RequestDetail;
import com.enoca.requestmanagement.enums.RequestType;
import org.springframework.stereotype.Component;

@Component
public class EquipmentRequestDetailHandler implements RequestDetailHandler {

    @Override
    public RequestType supportedType() {
        return RequestType.EQUIPMENT;
    }

    @Override
    public RequestDetail toEntity(CreateRequestDto dto) {
        EquipmentRequestDetail detail = new EquipmentRequestDetail();
        apply(detail, (CreateEquipmentRequestDto) dto);
        return detail;
    }

    @Override
    public void updateEntity(RequestDetail existing, CreateRequestDto dto) {
        apply((EquipmentRequestDetail) existing, (CreateEquipmentRequestDto) dto);
    }

    @Override
    public RequestDetailResponse toResponse(RequestDetail detail) {
        return EquipmentDetailResponse.from((EquipmentRequestDetail) detail);
    }

    private void apply(EquipmentRequestDetail detail, CreateEquipmentRequestDto dto) {
        detail.setEquipmentType(dto.equipmentType());
        detail.setEstimatedCost(dto.estimatedCost());
        detail.setQuantity(dto.quantity());
    }
}
