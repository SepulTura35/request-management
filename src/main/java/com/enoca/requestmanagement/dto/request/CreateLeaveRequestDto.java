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

        @NotBlank(message = "Açıklama zorunludur")
        @Size(max = 1000, message = "Açıklama en fazla 1000 karakter olabilir")
        String description,

        Priority priority,

        @NotNull(message = "İzin tipi zorunludur")
        LeaveType leaveType,

        @NotNull(message = "Başlangıç tarihi zorunludur")
        LocalDate startDate,

        @NotNull(message = "Bitiş tarihi zorunludur")
        LocalDate endDate

) implements CreateRequestDto {
}
