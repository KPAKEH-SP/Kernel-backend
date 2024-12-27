package ru.lcp.kernel.dtos;

import lombok.Data;
import ru.lcp.kernel.entities.Chat;

import java.util.List;
import java.util.UUID;

@Data
public class PublicChat {
    private List<PublicUser> users;
    private UUID chatId;
    private String chatName;

    public PublicChat(Chat chat) {
        chatId = chat.getId();
        users = chat.getParticipants().stream().map(
                chatParticipant -> new PublicUser(chatParticipant.getUser())).toList();
        chatName = chat.getChatName();
    }
}
