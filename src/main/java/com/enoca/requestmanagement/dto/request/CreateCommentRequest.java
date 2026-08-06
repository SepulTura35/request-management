package com.enoca.requestmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(

        @NotBlank(message = "Yorum içeriği zorunludur")
        @Size(max = 1000, message = "Yorum en fazla 1000 karakter olabilir")
        String content
) {
}
