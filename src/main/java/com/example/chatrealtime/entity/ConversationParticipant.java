package com.example.chatrealtime.entity;

import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatrealtime.enums.ConversationParticipantRole;

@Entity
@Table(name = "conversation_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ConversationParticipant.ConversationParticipantId.class)
public class ConversationParticipant {

    @Id
    @Column(name = "conversation_id")
    private UUID conversationId;

    @Id
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ConversationParticipantRole role = ConversationParticipantRole.MEMBER;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "is_muted")
    private Boolean isMuted = false;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationParticipantId implements Serializable {
        private UUID conversationId;
        private UUID userId;
    }
}