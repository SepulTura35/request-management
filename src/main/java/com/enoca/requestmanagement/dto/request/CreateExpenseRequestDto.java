package com.enoca.requestmanagement.dto.request;

import com.enoca.requestmanagement.enums.ExpenseCategory;
import com.enoca.requestmanagement.enums.Priority;
import com.enoca.requestmanagement.enums.RequestType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateExpenseRequestDto(

        @NotNull(message = "Talep tipi zorunludur")
        RequestType requestType,

        @NotBlank(message = "Aciklama zorunludur")
        @Size(max = 1000, message = "Aciklama en fazla 1000 karakter olabilir")
        String description,

        Priority priority,

        @NotNull(message = "Tutar zorunludur")
        @DecimalMin(value = "0.01", message = "Tutar sifirdan buyuk olmalidir")
        @Digits(integer = 10, fraction = 2, message = "Tutar en fazla 2 ondalik basamak icerebilir")
        BigDecimal amount,

        @NotNull(message = "Masraf kategorisi zorunludur")
        ExpenseCategory expenseCategory,

        @Size(max = 50, message = "Fis numarasi en fazla 50 karakter olabilir")
        String receiptNumber

) implements CreateRequestDto {
}
