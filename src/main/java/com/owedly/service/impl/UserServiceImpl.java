package com.owedly.service.impl;

import com.owedly.dto.response.UserResponse;
import com.owedly.entity.User;
import com.owedly.exception.ResourceNotFoundException;
import com.owedly.mapper.UserMapper;
import com.owedly.repository.UserRepository;
import com.owedly.service.UserService;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(
            UserRepository userRepository,
            UserMapper userMapper) {

        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        return userMapper.toResponse(user);
    }
}