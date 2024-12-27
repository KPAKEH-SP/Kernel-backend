package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.entities.Chat;
import ru.lcp.kernel.entities.ChatParticipant;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.enums.ChatRoles;
import ru.lcp.kernel.repositories.ChatParticipantRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatParticipantService {
    private final ChatParticipantRepository chatParticipantRepository;

    @Transactional
    public void addParticipantToChat(Chat chat, User user, ChatRoles role) {
        ChatParticipant participant = new ChatParticipant();
        participant.setChat(chat);
        participant.setUser(user);
        participant.setRole(role);
        chatParticipantRepository.save(participant);
    }

    public List<Chat> findChatsForUser(User user) {
        return chatParticipantRepository.findChatsByUserId(user.getId());
    }
}
