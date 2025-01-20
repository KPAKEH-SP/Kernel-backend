package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.dtos.CreateGroupChatRequest;
import ru.lcp.kernel.dtos.ChangeChatNameRequest;
import ru.lcp.kernel.dtos.PublicChat;
import ru.lcp.kernel.dtos.Username;
import ru.lcp.kernel.entities.Chat;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.enums.ChatRoles;
import ru.lcp.kernel.exceptions.ChatNotFound;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.repositories.ChatRepository;
import ru.lcp.kernel.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserUtils userUtils;
    private final ChatParticipantService chatParticipantService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public ResponseEntity<?> createChat(String token, Username username) {
        User user;
        User friend;

        try {
            user = userUtils.getByToken(token);
            friend = userUtils.getByUsername(username.getUsername());
        } catch (UserNotFound | ChatNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        Optional<Chat> chatOptional = chatRepository.findChatByParticipants(user.getId(), friend.getId());

        if (chatOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(chatOptional.get().getId());
        }

        Chat chat = new Chat();
        chatRepository.save(chat);

        chatParticipantService.addParticipantToChat(chat, user, ChatRoles.MEMBER);
        chatParticipantService.addParticipantToChat(chat, friend, ChatRoles.MEMBER);

        simpMessagingTemplate.convertAndSend("/topic/user/chats/" + user.getUsername(), new PublicChat(chat));
        simpMessagingTemplate.convertAndSend("/topic/user/chats/" + friend.getUsername(), new PublicChat(chat));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Transactional
    public ResponseEntity<?> createGroupChat(String token, CreateGroupChatRequest membersUsernames) {
        User creator;
        List<User> members = new ArrayList<>();

        try {
            creator = userUtils.getByToken(token);

            for (String member : membersUsernames.getMembersUsernames()) {
                members.add(userUtils.getByUsername(member));
            }
        } catch (UserNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        Chat chat = new Chat();
        chat.setChatName(membersUsernames.getChatName());
        chatRepository.save(chat);
        simpMessagingTemplate.convertAndSend("/topic/user/chats/" + creator.getUsername(), new PublicChat(chat));

        chatParticipantService.addParticipantToChat(chat, creator, ChatRoles.CREATOR);
        for (User member : members) {
            chatParticipantService.addParticipantToChat(chat, member, ChatRoles.MEMBER);
            simpMessagingTemplate.convertAndSend("/topic/user/chats/" + member.getUsername(), new PublicChat(chat));
        }

        return ResponseEntity.status(HttpStatus.CREATED).build();
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

    public ResponseEntity<?> changeChatName(String token, ChangeChatNameRequest request) {
        Chat chat;
        User user;

        try {
            chat = getChatById(request.getId());
            user = userUtils.getByToken(token);
        } catch (ChatNotFound | UserNotFound e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

        boolean userIsCreator = chat.getParticipants().stream().anyMatch(chatParticipant ->
                chatParticipant.getUser().getUsername().equals(user.getUsername()) &&
                chatParticipant.getRole() == ChatRoles.CREATOR);

        if (!userIsCreator) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Not enough rights");
        }
        chat.setChatName(request.getNewChatName());
        chatRepository.save(chat);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
