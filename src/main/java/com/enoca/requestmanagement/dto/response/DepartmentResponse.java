package com.enoca.requestmanagement.dto.response;

import com.enoca.requestmanagement.entity.Department;

public record DepartmentResponse(
        Long id,
        String name,
        String code
) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getId(), department.getName(), department.getCode());
    }
}
