package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.RegisterRequest;
import com.infy.NeoBank.dto.response.UserResponse;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return mapToResponse(user);
    }

    public UserResponse updateProfile(Long userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setFullName(request.getFullName());
        return mapToResponse(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setActive(!user.isActive());
        return mapToResponse(userRepository.save(user));
    }

    private UserResponse mapToResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setEmail(user.getEmail());
        r.setFullName(user.getFullName());
        r.setRole(user.getRole());
        r.setActive(user.isActive());
        r.setCreatedAt(user.getCreatedAt());
        return r;
    }
}

