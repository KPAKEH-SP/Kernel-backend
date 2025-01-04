package ru.lcp.kernel.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class SignalAnswer {
    private UUID chatId;
    private String respondentToken;
    private String initiatorUsername;
    private String data;
}
