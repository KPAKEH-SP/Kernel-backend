package ru.lcp.kernel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationPatterns {
    NEW_FRIEND_REQUEST("New friend request from ");

    private final String message;
}
