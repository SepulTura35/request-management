package com.enoca.requestmanagement.dto.response.detail;

import com.enoca.requestmanagement.entity.detail.EquipmentRequestDetail;
import com.enoca.requestmanagement.enums.EquipmentType;

import java.math.BigDecimal;

public record EquipmentDetailResponse(
        EquipmentType equipmentType,
        BigDecimal estimatedCost,
        Integer quantity
) implements RequestDetailResponse {

    public static EquipmentDetailResponse from(EquipmentRequestDetail detail) {
        return new EquipmentDetailResponse(
                detail.getEquipmentType(),
                detail.getEstimatedCost(),
                detail.getQuantity());
    }
}
