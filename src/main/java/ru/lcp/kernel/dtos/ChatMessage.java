package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class ChatMessage {
    private String sender;
    private String content;
    private Long chatId;
}