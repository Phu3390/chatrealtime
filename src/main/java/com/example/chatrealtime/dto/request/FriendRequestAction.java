package com.example.chatrealtime.dto.request;

import java.util.UUID;

import com.example.chatrealtime.enums.FriendRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequestAction {
    private UUID requestId;
    private FriendRequestStatus status;
}
