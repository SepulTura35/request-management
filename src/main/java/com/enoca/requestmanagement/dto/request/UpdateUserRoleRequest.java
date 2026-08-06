package com.enoca.requestmanagement.dto.request;

import com.enoca.requestmanagement.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(

        @NotNull(message = "Rol zorunludur")
        Role role
) {
}
