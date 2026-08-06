package com.enoca.requestmanagement.entity.detail;

import com.enoca.requestmanagement.enums.EquipmentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "equipment_request_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EquipmentRequestDetail extends RequestDetail {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EquipmentType equipmentType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    @Column(nullable = false)
    private Integer quantity;
}
