package com.example.chatrealtime.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendResponse {
    private UUID requestId;
    private UUID senderId;
    private String senderName;
    private UUID receiverId;
    private String receiverName;
    private String status;
    private LocalDateTime createdAt;
}
