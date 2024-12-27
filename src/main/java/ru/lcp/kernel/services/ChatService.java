package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.dtos.PublicChat;
import ru.lcp.kernel.entities.Chat;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.enums.ChatRoles;
import ru.lcp.kernel.exceptions.ChatNotFound;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.repositories.ChatRepository;
import ru.lcp.kernel.utils.UserUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserUtils userUtils;
    private final ChatParticipantService chatParticipantService;

    @Transactional
    public ResponseEntity<?> createChat(String token, String username) {
        try {
            User user = userUtils.getByToken(token);
            User friend = userUtils.getByUsername(username);

            Optional<Chat> chatOptional = chatRepository.findChatByParticipants(user.getId(), friend.getId());

            if (chatOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(chatOptional.get().getId());
            }

            Chat chat = new Chat();
            chatRepository.save(chat);

            chatParticipantService.addParticipantToChat(chat, user, ChatRoles.MEMBER);
            chatParticipantService.addParticipantToChat(chat, friend, ChatRoles.MEMBER);

            return ResponseEntity.status(HttpStatus.CREATED).body(chat.getId());
        } catch (UserNotFound | ChatNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    public List<PublicChat> getChatsForUser(User user) {
        List<Chat> userChats = chatParticipantService.findChatsForUser(user);
        return userChats.stream().map(PublicChat::new).toList();
    }

    public ResponseEntity<?> getChatsForUserByToken(String token) {
        try {
            User user = userUtils.getByToken(token);
            return ResponseEntity.ok(getChatsForUser(user));
        } catch (UserNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    public Chat getChatById(UUID chatId) throws ChatNotFound {
        Optional<Chat> chatOpt = chatRepository.findById(chatId);

        if (chatOpt.isEmpty()) {
            throw new ChatNotFound();
        }

        return chatOpt.get();
    }
}
