package com.example.chatrealtime.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.example.chatrealtime.enums.ConversationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// response
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    private UUID conversationId;
    private ConversationType type;
    private String name;
    private LocalDateTime createdAt;
    private List<ParticipantResponse> participants;
}
