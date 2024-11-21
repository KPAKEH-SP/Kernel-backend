package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.dtos.CreateChatWithFriendRequest;
import ru.lcp.kernel.dtos.PublicChat;
import ru.lcp.kernel.dtos.UserPublicInfo;
import ru.lcp.kernel.entities.Chat;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.repositories.ChatRepository;
import ru.lcp.kernel.utils.JwtTokenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserService userService;

    @Transactional
    public ResponseEntity<?> createChatWithFriend(CreateChatWithFriendRequest createChatWithFriendRequest) {

        String username = jwtTokenUtils.getUsername(createChatWithFriendRequest.getToken());
        String friendUsername = createChatWithFriendRequest.getFriendUsername();

        Optional<User> userOpt = userService.findByUsername(username);
        Optional<User> friendOpt = userService.findByUsername(friendUsername);

        if (userOpt.isEmpty() || friendOpt.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }

        Optional<Long> existingChatId = chatRepository.findExistingChatIdBetweenUsers(userOpt.get().getId(), friendOpt.get().getId());

        if (existingChatId.isPresent()) {
            return ResponseEntity.ok(existingChatId.get());
        }

        Long chatId = System.currentTimeMillis();

        Chat chatForUser = new Chat();
        chatForUser.setChatId(chatId);
        chatForUser.setUser(userOpt.get());

        Chat chatForFriend = new Chat();
        chatForFriend.setChatId(chatId);
        chatForFriend.setUser(friendOpt.get());

        chatRepository.save(chatForUser);
        chatRepository.save(chatForFriend);

        return getChatsForUser(userOpt.get().getId());
    }

    public ResponseEntity<?> getChatsForUser(Integer userId) {
        List<Chat> chats = chatRepository.findByUserId(userId);
        List<PublicChat> publicChats = new ArrayList<>();

        for(Chat chat : chats) {
            Long chatId = chat.getChatId();
            List<Chat> chatUsers = chatRepository.findByChatId(chatId);

            PublicChat publicChat = new PublicChat();
            publicChat.setChatId(chatId);
            List<UserPublicInfo> publicUsers = new ArrayList<>();

            for(Chat chatUser : chatUsers) {
                UserPublicInfo userPublicInfo = new UserPublicInfo();
                userPublicInfo.setUsername(chatUser.getUser().getUsername());
                publicUsers.add(userPublicInfo);
            }

            publicChat.setUsers(publicUsers);
            publicChats.add(publicChat);
        }

        return ResponseEntity.ok(publicChats);
    }

    public ResponseEntity<?> getChatsForUser(String token) {
        String username = jwtTokenUtils.getUsername(token);
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();
        return getChatsForUser(user.getId());
    }
}
