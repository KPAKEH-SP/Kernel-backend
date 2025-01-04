package ru.lcp.kernel.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import ru.lcp.kernel.dtos.SignalAnswer;
import ru.lcp.kernel.dtos.SignalMessage;
import ru.lcp.kernel.services.SignalService;

@Controller
@RequiredArgsConstructor
public class SignalController {
    private final SignalService webRTCService;

    @MessageMapping("/webrtc/chat/candidate")
    public void handleCandidate(SignalMessage message) {
        webRTCService.handleCandidate(message);
    }

    @MessageMapping("/webrtc/chat/offer")
    public void handleOffer(SignalMessage message) {
        webRTCService.handleOffer(message);
    }

    @MessageMapping("/webrtc/chat/answer")
    public void handleAnswer(SignalAnswer answer) {
        webRTCService.handleAnswer(answer);
    }
}
