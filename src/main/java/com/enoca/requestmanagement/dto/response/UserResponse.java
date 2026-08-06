package com.enoca.requestmanagement.dto.response;

import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        Role role,
        Long departmentId,
        String departmentName,
        boolean active,
        LocalDateTime createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getDepartment().getId(),
                user.getDepartment().getName(),
                user.isActive(),
                user.getCreatedAt());
    }
}
