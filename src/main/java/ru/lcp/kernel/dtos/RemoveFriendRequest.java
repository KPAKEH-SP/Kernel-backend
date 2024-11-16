package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class RemoveFriendRequest {
    private String friendUsername;
    private String userToken;
}
