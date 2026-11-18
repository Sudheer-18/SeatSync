package com.example.demo.dto.auth;

import com.example.demo.model.UserEntity;

public record UserResponse(Long id, String name, String email, String role) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
