package com.taskmaster.service;

import com.taskmaster.dto.request.UpdateProfileRequest;
import com.taskmaster.dto.response.UserResponse;
import com.taskmaster.entity.User;
import com.taskmaster.exception.ResourceNotFoundException;
import com.taskmaster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser(String username) {
        return AuthService.mapToUserResponse(findByUsername(username));
    }

    @Transactional
    public UserResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = findByUsername(username);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        return AuthService.mapToUserResponse(userRepository.save(user));
    }

    public UserResponse getUserById(Long id) {
        return AuthService.mapToUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id)));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(AuthService::mapToUserResponse).collect(Collectors.toList());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
