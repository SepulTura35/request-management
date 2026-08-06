package com.enoca.requestmanagement.service;

import com.enoca.requestmanagement.dto.request.LoginRequest;
import com.enoca.requestmanagement.dto.request.RegisterRequest;
import com.enoca.requestmanagement.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
