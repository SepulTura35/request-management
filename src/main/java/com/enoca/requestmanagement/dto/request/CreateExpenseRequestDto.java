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

        @NotBlank(message = "Açıklama zorunludur")
        @Size(max = 1000, message = "Açıklama en fazla 1000 karakter olabilir")
        String description,

        Priority priority,

        @NotNull(message = "Tutar zorunludur")
        @DecimalMin(value = "0.01", message = "Tutar sıfırdan büyük olmalıdır")
        @Digits(integer = 10, fraction = 2, message = "Tutar en fazla 2 ondalık basamak içerebilir")
        BigDecimal amount,

        @NotNull(message = "Masraf kategorisi zorunludur")
        ExpenseCategory expenseCategory,

        @Size(max = 50, message = "Fiş numarası en fazla 50 karakter olabilir")
        String receiptNumber

) implements CreateRequestDto {
}
