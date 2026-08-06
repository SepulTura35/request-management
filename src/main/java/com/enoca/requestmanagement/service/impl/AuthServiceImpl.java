package com.enoca.requestmanagement.service.impl;

import com.enoca.requestmanagement.dto.request.LoginRequest;
import com.enoca.requestmanagement.dto.request.RegisterRequest;
import com.enoca.requestmanagement.dto.response.AuthResponse;
import com.enoca.requestmanagement.entity.Department;
import com.enoca.requestmanagement.entity.User;
import com.enoca.requestmanagement.enums.AuditAction;
import com.enoca.requestmanagement.enums.Role;
import com.enoca.requestmanagement.event.AuditEvent;
import com.enoca.requestmanagement.exception.BusinessRuleException;
import com.enoca.requestmanagement.exception.ResourceNotFoundException;
import com.enoca.requestmanagement.repository.DepartmentRepository;
import com.enoca.requestmanagement.repository.UserRepository;
import com.enoca.requestmanagement.security.JwtTokenProvider;
import com.enoca.requestmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Bu e-posta adresi zaten kayıtlı: " + request.email());
        }

        Department department = departmentRepository.findByCode(request.departmentCode())
                .orElseThrow(() -> ResourceNotFoundException.of("Departman", request.departmentCode()));

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.EMPLOYEE)
                .department(department)
                .active(true)
                .build();

        User saved = userRepository.save(user);

        eventPublisher.publishEvent(AuditEvent.of(
                AuditAction.USER_REGISTERED, "User", saved.getId(), saved.getId(),
                "Yeni kullanıcı kaydı: " + saved.getEmail()));

        return toAuthResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmailWithDepartment(request.email())
                .orElseThrow(() -> ResourceNotFoundException.of("Kullanici", request.email()));

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return AuthResponse.bearer(
                token,
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getDepartment().getName());
    }
}
