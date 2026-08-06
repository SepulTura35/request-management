package com.enoca.requestmanagement.dto.request;

import com.enoca.requestmanagement.enums.AccessLevel;
import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccessPermissionRequestDto(

        @NotNull(message = "Talep tipi zorunludur")
        RequestType requestType,

        @NotBlank(message = "Aciklama zorunludur")
        @Size(max = 1000, message = "Aciklama en fazla 1000 karakter olabilir")
        String description,

        Priority priority,

        @NotBlank(message = "Sistem adi zorunludur")
        @Size(max = 100, message = "Sistem adi en fazla 100 karakter olabilir")
        String systemName,

        @NotNull(message = "Erisim seviyesi zorunludur")
        AccessLevel accessLevel,

        @NotBlank(message = "Gerekce zorunludur")
        @Size(max = 500, message = "Gerekce en fazla 500 karakter olabilir")
        String justification

) implements CreateRequestDto {
}
