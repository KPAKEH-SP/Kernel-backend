package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class ChatResponse {
    private String sender;
    private String content;
    private Long chatId;
    private String timestamp;
}