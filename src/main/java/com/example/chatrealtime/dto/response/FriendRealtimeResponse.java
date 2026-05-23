package com.example.chatrealtime.dto.response;

import java.util.UUID;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRealtimeResponse {

    private UUID requestId;

    private UserResponse sender;

    // private String senderId;

    // private String senderName;

    // private String senderAvatar;
    
}