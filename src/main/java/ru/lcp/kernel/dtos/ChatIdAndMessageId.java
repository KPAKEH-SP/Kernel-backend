package ru.lcp.kernel.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class ChatIdAndMessageId {
    UUID chatId;
    UUID messageId;
}
