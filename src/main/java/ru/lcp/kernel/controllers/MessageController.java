package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.lcp.kernel.dtos.ChatRequest;
import ru.lcp.kernel.dtos.ChatResponse;
import ru.lcp.kernel.dtos.ChatIdAndMessageId;
import ru.lcp.kernel.services.MessageService;

import java.util.List;
import java.util.UUID;


@Controller
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @MessageMapping("/chat/{chatId}")
    @SendTo("/topic/chat/{chatId}")
    public ChatResponse handleMessage(@DestinationVariable UUID chatId, ChatRequest chatRequest) {
        return messageService.saveMessage(chatId, chatRequest);
    }

    @MessageMapping("/chat/history/{chatId}")
    @SendTo("/topic/chat/history/{chatId}")
    public List<ChatResponse> sendChatHistory(@DestinationVariable UUID chatId) {
        return messageService.getMessages(chatId);
    }

    @PostMapping("api/messages/delete")
    public ResponseEntity<?> deleteMessage(@RequestHeader("X-Token") String token, @RequestBody ChatIdAndMessageId id) {
        return messageService.deleteMessage(token, id.getMessageId());
    }
}