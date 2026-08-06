package com.enoca.requestmanagement.controller;

import com.enoca.requestmanagement.dto.response.UserResponse;
import com.enoca.requestmanagement.security.CustomUserDetails;
import com.enoca.requestmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Kullanici", description = "Oturum açan kullanıcının kendi bilgileri")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Oturum açan kullanıcının profilini getirir")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(userService.findById(principal.getId()));
    }
}
