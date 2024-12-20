package ru.lcp.kernel.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class ChatResponse {
    private UUID messageId;
    private String sender;
    private String content;
    private UUID chatId;
    private String timestamp;
}