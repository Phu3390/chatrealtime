package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {


    // List<Conversation> findByType(String type);

    // List<Conversation> findByNameContainingIgnoreCase(String keyword);
}