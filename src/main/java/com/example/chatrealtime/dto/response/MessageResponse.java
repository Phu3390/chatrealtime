package com.example.chatrealtime.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatrealtime.entity.Conversation;
import com.example.chatrealtime.enums.MessageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private UUID id;
    private Conversation conversation;
    private UserResponse sender;
    private String content;
    private MessageType messageType;
    private LocalDateTime createdAt;
}
