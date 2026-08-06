package com.enoca.requestmanagement.dto.request;

import com.enoca.requestmanagement.enums.EquipmentType;
import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateEquipmentRequestDto(

        @NotNull(message = "Talep tipi zorunludur")
        RequestType requestType,

        @NotBlank(message = "Aciklama zorunludur")
        @Size(max = 1000, message = "Aciklama en fazla 1000 karakter olabilir")
        String description,

        Priority priority,

        @NotNull(message = "Ekipman tipi zorunludur")
        EquipmentType equipmentType,

        @NotNull(message = "Tahmini maliyet zorunludur")
        @DecimalMin(value = "0.01", message = "Tahmini maliyet sifirdan buyuk olmalidir")
        @Digits(integer = 10, fraction = 2, message = "Tahmini maliyet en fazla 2 ondalik basamak icerebilir")
        BigDecimal estimatedCost,

        @NotNull(message = "Adet zorunludur")
        @Min(value = 1, message = "Adet en az 1 olmalidir")
        @Max(value = 100, message = "Adet en fazla 100 olabilir")
        Integer quantity

) implements CreateRequestDto {
}
