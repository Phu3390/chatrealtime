package com.example.chatrealtime.dto.request;

import java.util.List;
import java.util.UUID;

import com.example.chatrealtime.enums.ConversationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateConversationRequest {
    private String avatarGroup;
    private ConversationType type;
    private String name;
    private List<UUID> participantIds;
}
