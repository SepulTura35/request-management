package com.enoca.requestmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "E-posta zorunludur")
        @Email(message = "Gecerli bir e-posta adresi giriniz")
        String email,

        @NotBlank(message = "Sifre zorunludur")
        String password
) {
}
