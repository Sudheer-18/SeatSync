package com.example.demo.repository;

import java.util.List;
import com.example.demo.model.ChatMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByUser_IdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);
}
