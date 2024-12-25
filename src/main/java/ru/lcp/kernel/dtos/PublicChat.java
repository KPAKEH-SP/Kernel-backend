package ru.lcp.kernel.dtos;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PublicChat {
    private List<PublicUser> users;
    private UUID chatId;
}
