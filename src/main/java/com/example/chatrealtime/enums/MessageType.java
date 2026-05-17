package com.example.chatrealtime.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    TEXT("text"),
    IMAGE("image"),
    FILE("file");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }
}
