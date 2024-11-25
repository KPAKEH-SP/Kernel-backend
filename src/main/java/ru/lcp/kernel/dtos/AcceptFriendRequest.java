package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class AcceptFriendRequest {
    private String token;
    private String friendUsername;
}
