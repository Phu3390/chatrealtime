package com.example.chatrealtime.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatrealtime.entity.User;
import com.example.chatrealtime.enums.ConversationType;
import com.example.chatrealtime.enums.MessageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationSummaryResponse {
    private UUID conversationId;
    private ConversationType type;
    private String name;
    private UserResponse targetUser; // Dành cho 1-1 chat
    private String lastMessage;
    private MessageType lastMessageType;
    private UUID lastSenderId;
    private String lastSenderName;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
}