package com.example.chatrealtime.repository;

import com.example.chatrealtime.entity.Message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  // List<Message> findByConversationOrderByCreatedAtAsc(Conversation
  // conversation);

  // List<Message> findTop20ByConversationOrderByCreatedAtDesc(Conversation
  // conversation);

  // long countByConversation(Conversation conversation);

  List<Message> findByConversation_IdOrderByCreatedAtAsc(UUID conversationId);

  Optional<Message> findTopByConversation_IdOrderByCreatedAtDesc(UUID conversationId);

  @Query("""
          SELECT COUNT(m)
          FROM Message m
          WHERE m.conversation.id = :conversationId
            AND m.sender.id <> :userId
            AND m.createdAt > :joinedAt
      """)
  int countUnreadMessages(
      @Param("conversationId") UUID conversationId,
      @Param("userId") UUID userId,
      @Param("joinedAt") LocalDateTime joinedAt);
}