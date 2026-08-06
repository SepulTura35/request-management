package com.enoca.requestmanagement.controller;

import com.enoca.requestmanagement.dto.request.CreateDepartmentRequest;
import com.enoca.requestmanagement.dto.request.CreateUserRequest;
import com.enoca.requestmanagement.dto.request.UpdateUserRoleRequest;
import com.enoca.requestmanagement.dto.response.AuditLogResponse;
import com.enoca.requestmanagement.dto.response.DepartmentResponse;
import com.enoca.requestmanagement.dto.response.PageResponse;
import com.enoca.requestmanagement.dto.response.UserResponse;
import com.enoca.requestmanagement.service.AuditService;
import com.enoca.requestmanagement.service.DepartmentService;
import com.enoca.requestmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Yonetim", description = "Kullanici ve departman yonetimi (yalnizca ADMIN)")
public class AdminController {

    private final UserService userService;
    private final DepartmentService departmentService;
    private final AuditService auditService;

    @GetMapping("/users")
    @Operation(summary = "Kullanicilari sayfali olarak listeler")
    public ResponseEntity<PageResponse<UserResponse>> listUsers(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Tek bir kullaniciyi getirir")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping("/users")
    @Operation(summary = "Belirtilen rolde yeni kullanici olusturur")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Kullanicinin rolunu degistirir")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Kullaniciyi aktif veya pasif yapar")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id,
                                                         @RequestParam boolean active) {
        return ResponseEntity.ok(userService.setActive(id, active));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Sistem genelindeki islem kayitlarini sayfali olarak listeler")
    public ResponseEntity<PageResponse<AuditLogResponse>> listAuditLogs(
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(auditService.findAll(pageable));
    }

    @GetMapping("/departments")
    @Operation(summary = "Tum departmanlari listeler")
    public ResponseEntity<List<DepartmentResponse>> listDepartments() {
        return ResponseEntity.ok(departmentService.findAll());
    }

    @PostMapping("/departments")
    @Operation(summary = "Yeni departman olusturur")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
    }
}
