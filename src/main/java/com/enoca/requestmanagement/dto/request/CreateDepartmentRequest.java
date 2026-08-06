package com.enoca.requestmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequest(

        @NotBlank(message = "Departman adi zorunludur")
        @Size(max = 100, message = "Departman adi en fazla 100 karakter olabilir")
        String name,

        @NotBlank(message = "Departman kodu zorunludur")
        @Size(max = 20, message = "Departman kodu en fazla 20 karakter olabilir")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "Departman kodu yalnizca buyuk harf, rakam ve alt cizgi icerebilir")
        String code
) {
}
