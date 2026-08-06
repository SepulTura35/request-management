package com.enoca.requestmanagement.dto.response;

import com.enoca.requestmanagement.enums.Role;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String email,
        String fullName,
        Role role,
        String departmentName
) {

    public static AuthResponse bearer(String token, Long userId, String email,
                                      String fullName, Role role, String departmentName) {
        return new AuthResponse(token, "Bearer", userId, email, fullName, role, departmentName);
    }
}
