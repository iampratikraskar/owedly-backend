package com.owedly.service;

import com.owedly.dto.request.LoginRequest;
import com.owedly.dto.request.RegisterRequest;
import com.owedly.dto.response.AuthResponse;
import com.owedly.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}