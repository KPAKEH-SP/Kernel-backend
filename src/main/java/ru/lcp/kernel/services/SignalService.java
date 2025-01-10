package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.dtos.*;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.enums.CallMessageType;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.utils.UserUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserUtils userUtils;

    @Transactional
    public void handleCandidate(SignalMessage message, String respondentUsername) {
        System.out.println(respondentUsername);
        try {
            User initiator = userUtils.getByToken(message.getSenderToken());

            simpMessagingTemplate.convertAndSend("/topic/webrtc/candidate/user/" + respondentUsername, message.getData());
            log.debug("\n>>> Sent ICE candidate: {}\n>>>To user: {}", message.getData(), respondentUsername);
        } catch (UserNotFound e) {
            log.warn(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void handleOffer(SignalMessage message, String respondentUsername) {

        try {
            User user = userUtils.getByToken(message.getSenderToken());

            simpMessagingTemplate.convertAndSend("/topic/webrtc/offer/user/" + respondentUsername, message.getData());
            log.debug("\n>>> Sent offer: {}\n>>>To user: {}", message.getData(), respondentUsername);
        } catch (UserNotFound e) {
            log.warn(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void handleAnswer(SignalMessage message, String respondentUsername) {
        try {
            User sender = userUtils.getByToken(message.getSenderToken());
            simpMessagingTemplate.convertAndSend("/topic/webrtc/answer/user/" + respondentUsername, message.getData());
            log.debug("\n>>> Sent answer: {}\n>>>To user: {}", message.getData(), respondentUsername);
        } catch (UserNotFound e) {
            log.warn(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public CallMessage handleCall(CallMessage message, String respondentUsername) {
        CallMessage result = new CallMessage();

        try {
            User sender = userUtils.getByToken(message.getSender());
            result.setSender(sender.getUsername());
            result.setType(message.getType());
        } catch (UserNotFound e) {
            log.warn(e.getMessage());
            result.setType(CallMessageType.ERROR);
        }

        return result;
    }
}
