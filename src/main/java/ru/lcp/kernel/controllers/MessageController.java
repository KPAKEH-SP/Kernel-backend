package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import ru.lcp.kernel.dtos.ChatRequest;
import ru.lcp.kernel.dtos.ChatResponse;
import ru.lcp.kernel.services.MessageService;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @MessageMapping("/chat/{chatId}")
    @SendTo("/topic/chat/{chatId}")
    public ChatResponse handleMessage(@DestinationVariable Long chatId, ChatRequest chatRequest) {
        return messageService.saveMessage(chatId, chatRequest);
    }

    @MessageMapping("/chat/history/{chatId}")
    @SendTo("/topic/chat/history/{chatId}")
    public List<ChatResponse> sendChatHistory(@DestinationVariable Long chatId) {
        return messageService.getMessages(chatId);
    }
}