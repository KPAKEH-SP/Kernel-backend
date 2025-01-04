package ru.lcp.kernel.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class SignalOffer {
    private UUID chatId;
    private String initiatorUsername;
    private String data;
}
