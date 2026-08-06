package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.dto.request.CreateDepartmentRequest;
import com.enoca.requestmanagement.dto.response.DepartmentResponse;
import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.exception.ResourceNotFoundException;
import com.enoca.requestmanagement.repository.DepartmentRepository;
import com.enoca.requestmanagement.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        if (departmentRepository.existsByCode(request.code())) {
            throw new BusinessRuleException("Bu departman kodu zaten kullaniliyor: " + request.code());
        }

        Department department = Department.builder()
                .name(request.name())
                .code(request.code())
                .build();

        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll(Sort.by("name")).stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse findById(Long id) {
        return departmentRepository.findById(id)
                .map(DepartmentResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("Departman", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Department getEntityByCode(String code) {
        return departmentRepository.findByCode(code)
                .orElseThrow(() -> ResourceNotFoundException.of("Departman", code));
    }
}
