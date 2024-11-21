package ru.lcp.kernel.dtos;

import lombok.Data;

import java.util.List;

@Data
public class PublicChat {
    private List<UserPublicInfo> users;
    private Long chatId;
}
