package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lcp.kernel.dtos.CreateGroupChatRequest;
import ru.lcp.kernel.dtos.Username;
import ru.lcp.kernel.services.ChatService;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/create")
    public ResponseEntity<?> createChat(@RequestHeader("X-Token") String token, @RequestBody Username username) {
        return chatService.createChat(token, username);
    }

    @PostMapping("/create/group")
    public ResponseEntity<?> createGroupChat(@RequestHeader("X-Token") String token, @RequestBody CreateGroupChatRequest membersUsernames) {
        return chatService.createGroupChat(token, membersUsernames);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAllChats(@RequestHeader("X-Token") String token) {
        return chatService.getChatsForUserByToken(token);
    }
}
