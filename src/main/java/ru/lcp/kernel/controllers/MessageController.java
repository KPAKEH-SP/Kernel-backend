package ru.lcp.kernel.controllers;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import ru.lcp.kernel.dtos.ChatMessage;
import ru.lcp.kernel.dtos.ChatResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class MessageController {

    @MessageMapping("/chat/{chatId}")
    @SendTo("/topic/chat/{chatId}")
    public ChatResponse sendMessage(@DestinationVariable Long chatId, ChatMessage chatMessage) {
        ChatResponse response = new ChatResponse();
        response.setSender(chatMessage.getSender());
        response.setContent(chatMessage.getContent());
        response.setChatId(chatId);
        response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return response;
    }
}