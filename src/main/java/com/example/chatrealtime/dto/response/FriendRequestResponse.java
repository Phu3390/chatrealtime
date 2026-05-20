package com.example.chatrealtime.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatrealtime.enums.FriendRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestResponse {
    private UUID id;
    private UserResponse sender;
    private UserResponse receiver;
    private FriendRequestStatus status;
    private LocalDateTime createdAt;

}
