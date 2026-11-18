package com.example.demo.services;

import com.example.demo.dto.auth.LoginRequest;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.dto.auth.RegisterRequest;
import com.example.demo.dto.auth.UserResponse;
import com.example.demo.exception.ApiException;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.JwtService;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService tokens;

    public AuthService(UserRepository users, PasswordEncoder passwords, JwtService tokens) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String name = request.name().trim();
        if (name.length() < 2) throw ApiException.badRequest("name must be between 2 and 100 characters.");
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmailIgnoreCase(email)) throw ApiException.conflict("Email already exists.");
        UserEntity user = new UserEntity(name, email, passwords.encode(request.password()));
        return UserResponse.from(users.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserEntity user = users.findByEmailIgnoreCase(request.email().trim())
            .filter(account -> account.getPasswordHash() != null
                && passwords.matches(request.password(), account.getPasswordHash()))
            .orElseThrow(() -> ApiException.unauthorized("Invalid email or password."));
        return LoginResponse.from(tokens.issue(user), UserResponse.from(user));
    }

    @Transactional(readOnly = true)
    public UserResponse me(AuthenticatedUser principal) {
        return users.findById(principal.id())
            .map(UserResponse::from)
            .orElseThrow(() -> ApiException.unauthorized("Invalid or expired token."));
    }
}
