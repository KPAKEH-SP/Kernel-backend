package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class CreateChatWithFriendRequest {
    private String token;
    private String friendUsername;
}
