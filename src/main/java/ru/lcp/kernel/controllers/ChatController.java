package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lcp.kernel.dtos.CreateChatWithFriendRequest;
import ru.lcp.kernel.dtos.UserInfoRequest;
import ru.lcp.kernel.services.ChatService;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/create")
    public ResponseEntity<?> createChat(@RequestBody CreateChatWithFriendRequest request) {
        return chatService.createChatWithFriend(request);
    }

    @PostMapping("/get")
    public ResponseEntity<?> getAllChats(@RequestBody UserInfoRequest request) {
        return chatService.getChatsForUser(request.getToken());
    }
}
