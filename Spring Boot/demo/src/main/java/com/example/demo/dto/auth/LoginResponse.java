package com.example.demo.dto.auth;

import com.example.demo.security.JwtService.TokenResult;

public record LoginResponse(String token, String tokenType, long expiresIn, UserResponse user) {
    public static LoginResponse from(TokenResult token, UserResponse user) {
        return new LoginResponse(token.token(), token.tokenType(), token.expiresIn(), user);
    }
}
