package com.example.chatrealtime.enums;

import lombok.Getter;

@Getter
public enum FriendRequestStatus {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected");

    private final String value;

    FriendRequestStatus(String value) {
        this.value = value;
    }
}
