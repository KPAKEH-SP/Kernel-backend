package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class AddFriendRequest {
    private String token;
    private String friendName;
}
