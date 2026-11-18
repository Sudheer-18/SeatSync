package com.example.demo.services;

import com.example.demo.model.Role;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Component
public class ApplicationDataInitializer implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final String adminEmail;
    private final String adminPassword;

    public ApplicationDataInitializer(
        UserRepository users,
        PasswordEncoder passwords,
        @Value("${seatsync.admin.email}") String adminEmail,
        @Value("${seatsync.admin.password}") String adminPassword
    ) {
        this.users = users;
        this.passwords = passwords;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        migrateLegacyUsers();
        seedAdminIfConfigured();
    }

    private void migrateLegacyUsers() {
        for (UserEntity user : users.findAll()) {
            if (user.getPasswordHash() == null && user.getLegacyPassword() != null) {
                user.setPasswordHash(passwords.encode(user.getLegacyPassword()));
            }
            if (user.getLegacyPassword() != null) user.clearLegacyPassword();
            if (user.getRole() == null) user.setRole(Role.USER);
        }
        users.flush();
    }

    private void seedAdminIfConfigured() {
        if (adminEmail.isBlank() && adminPassword.isBlank()) return;
        if (adminEmail.isBlank() || adminPassword.length() < 12) {
            throw new IllegalStateException("Configure both admin email and a password of at least 12 characters.");
        }
        if (users.existsByEmailIgnoreCase(adminEmail.trim())) return;
        UserEntity admin = new UserEntity("SeatSync Administrator",
            adminEmail.trim().toLowerCase(Locale.ROOT), passwords.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        users.save(admin);
    }
}
