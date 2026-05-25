package com.example.chatrealtime.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatrealtime.enums.ConversationParticipantRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantResponse {
    private UUID userId;
    private String fullName;
    private String avatar;
    private ConversationParticipantRole role;
    private Boolean isMuted;
    private LocalDateTime lastReadAt;
    private LocalDateTime joinedAt;
}
