package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lcp.kernel.dtos.Username;
import ru.lcp.kernel.services.ChatService;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class PersonalChatController {
    private final ChatService chatService;

    @PostMapping("/create")
    public ResponseEntity<?> createChat(@RequestHeader("X-Token") String token, @RequestBody Username username) {
        return chatService.createChat(token, username.getUsername());
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAllChats(@RequestHeader("X-Token") String token) {
        return chatService.getChatsForUserByToken(token);
    }
}
