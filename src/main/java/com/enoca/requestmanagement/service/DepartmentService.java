package com.enoca.requestmanagement.service;

import com.enoca.requestmanagement.dto.request.CreateDepartmentRequest;
import com.enoca.requestmanagement.dto.response.DepartmentResponse;
import com.enoca.requestmanagement.entity.Department;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse create(CreateDepartmentRequest request);

    List<DepartmentResponse> findAll();

    DepartmentResponse findById(Long id);

    Department getEntityByCode(String code);
}
