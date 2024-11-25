package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class PublicFriendship {
    private UserPublicInfo user;
    private String status;
    private UserPublicInfo pendingFrom;
}
