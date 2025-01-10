package ru.lcp.kernel.dtos;

import lombok.Data;
import ru.lcp.kernel.enums.CallMessageType;

@Data
public class CallMessage {
    private String sender;
    private CallMessageType type;
}
