package com.example.chatrealtime.enums;

import lombok.Getter;

@Getter
public enum ConversationParticipantRole {
    MEMBER("member"),
    ADMIN("admin");

    private final String value;

    ConversationParticipantRole(String value) {
        this.value = value;
    }
}
