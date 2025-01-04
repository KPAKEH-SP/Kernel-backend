package ru.lcp.kernel.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class SignalMessage {
    private UUID chatId;
    private String initiatorToken;
    private String data;
}
