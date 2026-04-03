package com.taskmaster.service;

import com.taskmaster.dto.request.LoginRequest;
import com.taskmaster.dto.request.RegisterRequest;
import com.taskmaster.dto.response.AuthResponse;
import com.taskmaster.dto.response.UserResponse;
import com.taskmaster.entity.User;
import com.taskmaster.exception.BadRequestException;
import com.taskmaster.repository.UserRepository;
import com.taskmaster.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user authentication: registration and login.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.USER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: {}", savedUser.getUsername());
        return mapToUserResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication.getName());
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();

        log.info("User logged in: {}", user.getUsername());
        return AuthResponse.builder()
                .accessToken(accessToken).refreshToken(refreshToken)
                .tokenType("Bearer").user(mapToUserResponse(user)).build();
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken))
            throw new BadRequestException("Invalid or expired refresh token");
        String username = tokenProvider.getUsernameFromToken(refreshToken);
        String newAccessToken = tokenProvider.generateTokenFromUsername(username);
        User user = userRepository.findByUsername(username).orElseThrow();
        return AuthResponse.builder()
                .accessToken(newAccessToken).refreshToken(refreshToken)
                .tokenType("Bearer").user(mapToUserResponse(user)).build();
    }

    public static UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId()).username(user.getUsername()).email(user.getEmail())
                .fullName(user.getFullName()).bio(user.getBio()).avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name()).createdAt(user.getCreatedAt()).build();
    }
}
