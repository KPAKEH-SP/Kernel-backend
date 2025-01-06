package ru.lcp.kernel.dtos;

import lombok.Data;
import ru.lcp.kernel.enums.CallMessageType;

import java.util.UUID;

@Data
public class CallMessage {
    private UUID chatId;
    private String senderToken;
    private String respondentUsername;
    private CallMessageType type;
}
