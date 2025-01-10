package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import ru.lcp.kernel.dtos.CallMessage;
import ru.lcp.kernel.dtos.SignalMessage;
import ru.lcp.kernel.services.SignalService;

@Controller
@RequiredArgsConstructor
public class SignalController {
    private final SignalService signalService;

    @MessageMapping("/webrtc/candidate/user/{username}")
    @SendTo("/topic/webrtc/candidate/user/{username}")
    public String handleCandidate(SignalMessage message, @DestinationVariable String username) {
        return signalService.handleCandidate(message, username);
    }

    @MessageMapping("/webrtc/offer/user/{username}")
    @SendTo("/topic/webrtc/offer/user/{username}")
    public String handleOffer(SignalMessage message, @DestinationVariable String username) {
        return signalService.handleOffer(message, username);
    }

    @MessageMapping("/webrtc/answer/user/{username}")
    @SendTo("/topic/webrtc/answer/user/{username}")
    public String handleAnswer(SignalMessage message, @DestinationVariable String username) {
        return signalService.handleAnswer(message, username);
    }

    @MessageMapping("/call/user/{username}")
    @SendTo("/topic/call/user/{username}")
    public CallMessage handleCall(CallMessage message, @DestinationVariable String username) {
        return signalService.handleCall(message, username);
    }
}
