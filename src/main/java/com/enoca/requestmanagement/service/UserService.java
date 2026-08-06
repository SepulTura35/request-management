package com.enoca.requestmanagement.service;

import com.enoca.requestmanagement.dto.request.CreateUserRequest;
import com.enoca.requestmanagement.dto.request.UpdateUserRoleRequest;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.dto.response.UserResponse;
import com.enoca.requestmanagement.entity.User;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    PageResponse<UserResponse> findAll(Pageable pageable);

    UserResponse findById(Long id);

    UserResponse updateRole(Long id, UpdateUserRoleRequest request);

    UserResponse setActive(Long id, boolean active);

    User getEntityById(Long id);

    User getEntityByEmail(String email);
}
