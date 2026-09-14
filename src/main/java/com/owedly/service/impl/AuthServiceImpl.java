package com.owedly.service.impl;

import com.owedly.dto.request.LoginRequest;
import com.owedly.dto.request.RegisterRequest;
import com.owedly.dto.response.AuthResponse;
import com.owedly.dto.response.UserResponse;
import com.owedly.entity.Role;
import com.owedly.entity.User;
import com.owedly.exception.EmailAlreadyExistsException;
import com.owedly.exception.InvalidCredentialsException;
import com.owedly.mapper.UserMapper;
import com.owedly.repository.UserRepository;
import com.owedly.security.JwtService;
import com.owedly.service.AuthService;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    @Override
    public UserResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists"
            );
        }

        User user = new User();

        user.setName(request.getName().trim());
        user.setEmail(email);

        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        user.setPassword(encodedPassword);
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"
                        )
                );

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .authorities(
                                "ROLE_" + user.getRole().name()
                        )
                        .build();

        String token = jwtService.generateToken(userDetails);

        UserResponse userResponse =
                userMapper.toResponse(user);

        return new AuthResponse(
                token,
                "Bearer",
                86400000L,
                userResponse
        );
    }
}