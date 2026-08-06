package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.dto.request.CreateUserRequest;
import com.enoca.requestmanagement.dto.request.UpdateUserRoleRequest;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.dto.response.UserResponse;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.exception.ResourceNotFoundException;
import com.enoca.requestmanagement.repository.UserRepository;
import com.enoca.requestmanagement.service.DepartmentService;
import com.enoca.requestmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentService departmentService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Bu e-posta adresi zaten kayıtlı: " + request.email());
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .department(departmentService.getEntityByCode(request.departmentCode()))
                .active(true)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable), UserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getEntityById(id));
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long id, UpdateUserRoleRequest request) {
        User user = getEntityById(id);
        user.setRole(request.role());
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse setActive(Long id, boolean active) {
        User user = getEntityById(id);
        user.setActive(active);
        return UserResponse.from(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Kullanici", id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getEntityByEmail(String email) {
        return userRepository.findByEmailWithDepartment(email)
                .orElseThrow(() -> ResourceNotFoundException.of("Kullanici", email));
    }
}
