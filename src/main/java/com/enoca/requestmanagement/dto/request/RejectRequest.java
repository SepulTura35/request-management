package com.enoca.requestmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectRequest(

        @NotBlank(message = "Red gerekçesi zorunludur")
        @Size(max = 1000, message = "Red gerekçesi en fazla 1000 karakter olabilir")
        String comment
) {
}
