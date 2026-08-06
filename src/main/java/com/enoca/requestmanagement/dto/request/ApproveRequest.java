package com.enoca.requestmanagement.dto.request;

import jakarta.validation.constraints.Size;

public record ApproveRequest(

        @Size(max = 1000, message = "Aciklama en fazla 1000 karakter olabilir")
        String comment
) {
}
