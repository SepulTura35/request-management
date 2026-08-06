package com.enoca.requestmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Ad zorunludur")
        @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
        String firstName,

        @NotBlank(message = "Soyad zorunludur")
        @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
        String lastName,

        @NotBlank(message = "E-posta zorunludur")
        @Email(message = "Geçerli bir e-posta adresi giriniz")
        @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir")
        String email,

        @NotBlank(message = "Şifre zorunludur")
        @Size(min = 8, max = 100, message = "Şifre en az 8 karakter olmalıdır")
        String password,

        @NotBlank(message = "Departman kodu zorunludur")
        String departmentCode
) {
}
