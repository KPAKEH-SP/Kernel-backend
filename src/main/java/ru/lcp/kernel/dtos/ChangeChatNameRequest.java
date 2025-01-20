package ru.lcp.kernel.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class ChangeChatNameRequest {
    private UUID id;
    private String newChatName;
}
