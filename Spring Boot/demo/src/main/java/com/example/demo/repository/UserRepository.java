package com.example.demo.repository;

import java.util.List;
import java.util.Optional;
import com.example.demo.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<UserEntity> findByPasswordHashIsNullAndLegacyPasswordIsNotNull();
}


