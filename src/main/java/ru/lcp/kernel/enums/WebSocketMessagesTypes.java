package ru.lcp.kernel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WebSocketMessagesTypes {
    CONNECT,
    NEW_FRIEND_REQUEST;
}
