package com.owedly.service;

import com.owedly.dto.response.UserResponse;

public interface UserService {

    UserResponse getCurrentUser(String email);
}