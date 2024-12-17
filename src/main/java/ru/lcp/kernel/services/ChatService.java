package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.dtos.PublicChat;
import ru.lcp.kernel.dtos.TokenAndFriendUsername;
import ru.lcp.kernel.dtos.UserPublicInfo;
import ru.lcp.kernel.entities.Chat;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.exceptions.ApplicationError;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.repositories.ChatRepository;
import ru.lcp.kernel.utils.JwtTokenUtils;
import ru.lcp.kernel.utils.UserUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final JwtTokenUtils jwtTokenUtils;
    private final UserService userService;
    private final UserUtils userUtils;

    @Transactional
    public ResponseEntity<?> createChatWithFriend(TokenAndFriendUsername tokenAndFriendUsername) {
        try {
            User user = userUtils.getByToken(tokenAndFriendUsername.getToken());
            User friend = userUtils.getByUsername(tokenAndFriendUsername.getFriendUsername());

            Optional<Long> existingChatId = chatRepository.findExistingChatIdBetweenUsers(user.getId(), friend.getId());

            if (existingChatId.isPresent()) {
                return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), existingChatId.get().toString()), HttpStatus.CONFLICT);
            }

            Long chatId = System.currentTimeMillis();

            Chat chatForUser = new Chat();
            chatForUser.setChatId(chatId);
            chatForUser.setUser(user);

            Chat chatForFriend = new Chat();
            chatForFriend.setChatId(chatId);
            chatForFriend.setUser(friend);

            chatRepository.save(chatForUser);
            chatRepository.save(chatForFriend);

            return getChatsForUserById(user.getId());
        } catch (UserNotFound e) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getChatsForUserById(UUID userId) {
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

    public ResponseEntity<?> getChatsForUserByToken(String token) {
        String username = jwtTokenUtils.getUsername(token);
        Optional<User> userOpt = userService.findByUsername(username);

        if (userOpt.isEmpty()) {
            return new ResponseEntity<>(new ApplicationError(HttpStatus.BAD_REQUEST.value(), "User not found"), HttpStatus.NOT_FOUND);
        }

        User user = userOpt.get();
        return getChatsForUserById(user.getId());
    }

    public List<User> getChatUsers(Long chatId) {
        return chatRepository.findByChatId(chatId).stream().map(Chat::getUser).collect(Collectors.toList());
    }
}
