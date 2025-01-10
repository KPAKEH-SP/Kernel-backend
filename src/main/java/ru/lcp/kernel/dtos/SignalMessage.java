package ru.lcp.kernel.dtos;

import lombok.Data;

@Data
public class SignalMessage {
    private String senderToken;
    private String data;
}
