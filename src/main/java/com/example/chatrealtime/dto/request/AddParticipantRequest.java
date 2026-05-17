package com.example.chatrealtime.dto.request;

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
public class AddParticipantRequest {
    private UUID conversationId;
    private UUID userId;
    private ConversationParticipantRole role; 
}
