package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class ChatRequest {
    private String token;
    private String content;
}
