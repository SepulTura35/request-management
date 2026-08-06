package com.enoca.requestmanagement.dto.request;

import com.enoca.requestmanagement.enums.LeaveType;
import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateLeaveRequestDto(

        @NotNull(message = "Talep tipi zorunludur")
        RequestType requestType,

        @NotBlank(message = "Aciklama zorunludur")
        @Size(max = 1000, message = "Aciklama en fazla 1000 karakter olabilir")
        String description,

        Priority priority,

        @NotNull(message = "Izin tipi zorunludur")
        LeaveType leaveType,

        @NotNull(message = "Baslangic tarihi zorunludur")
        LocalDate startDate,

        @NotNull(message = "Bitis tarihi zorunludur")
        LocalDate endDate

) implements CreateRequestDto {
}
