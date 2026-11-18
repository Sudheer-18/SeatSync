package com.example.demo.repository;

import java.util.Optional;
import com.example.demo.model.ChatPendingActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatPendingActionRepository extends JpaRepository<ChatPendingActionEntity, Long> {
    Optional<ChatPendingActionEntity> findByUser_Id(Long userId);
    void deleteByUser_Id(Long userId);
}
