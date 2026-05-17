package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationParticipantRepository extends JpaRepository<
        ConversationParticipant,
        ConversationParticipant.ConversationParticipantId> {

    List<ConversationParticipant> findByUserId(UUID userId);

    List<ConversationParticipant> findByConversationId(UUID conversationId);

    boolean existsByConversationIdAndUserId(UUID conversationId, UUID userId);

    void deleteByConversationIdAndUserId(UUID conversationId, UUID userId);

    Optional<ConversationParticipant> findByConversationIdAndUserId(UUID conversationId, UUID userId);
}