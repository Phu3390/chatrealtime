package com.example.chatrealtime.dto.request;

import java.util.UUID;

import com.example.chatrealtime.enums.MessageType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {
    private UUID conversationId;
    private String content;
    private MessageType messageType;
}
