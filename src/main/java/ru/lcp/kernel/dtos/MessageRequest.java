package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class MessageRequest {
    private String token;
    private String content;
}
