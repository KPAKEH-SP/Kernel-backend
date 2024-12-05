package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lcp.kernel.dtos.TokenAndFriendUsername;
import ru.lcp.kernel.dtos.UserInfoRequest;
import ru.lcp.kernel.services.ChatService;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/create")
    public ResponseEntity<?> createChat(@RequestBody TokenAndFriendUsername tokenAndFriendUsername) {
        return chatService.createChatWithFriend(tokenAndFriendUsername);
    }

    @PostMapping("/get")
    public ResponseEntity<?> getAllChats(@RequestBody UserInfoRequest request) {
        return chatService.getChatsForUserByToken(request.getToken());
    }
}
