package com.enoca.requestmanagement.dto.request;

import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateRemoteWorkRequestDto(

        @NotNull(message = "Talep tipi zorunludur")
        RequestType requestType,

        @NotBlank(message = "Aciklama zorunludur")
        @Size(max = 1000, message = "Aciklama en fazla 1000 karakter olabilir")
        String description,

        Priority priority,

        @NotNull(message = "Baslangic tarihi zorunludur")
        LocalDate startDate,

        @NotNull(message = "Bitis tarihi zorunludur")
        LocalDate endDate,

        @NotBlank(message = "Calisma lokasyonu zorunludur")
        @Size(max = 150, message = "Calisma lokasyonu en fazla 150 karakter olabilir")
        String workLocation

) implements CreateRequestDto {
}
