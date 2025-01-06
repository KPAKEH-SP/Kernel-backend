package ru.lcp.kernel.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class CallAnswer {
    private UUID chatId;
    private String senderUsername;
}
