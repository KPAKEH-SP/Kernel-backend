package ru.lcp.kernel.dtos;

import lombok.Data;

import java.util.List;

@Data
public class CreateGroupChatRequest {
    private List<String> membersUsernames;
    private String chatName;
}
