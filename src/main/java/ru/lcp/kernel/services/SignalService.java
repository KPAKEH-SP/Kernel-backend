package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lcp.kernel.dtos.*;
import ru.lcp.kernel.entities.Chat;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.utils.UserUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalService {
    private final ChatService chatService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserUtils userUtils;

    @Transactional
    public void handleCandidate(SignalMessage message) {
        Chat chat = chatService.getChatById(message.getChatId());

        chat.getParticipants().forEach(participant -> {
            simpMessagingTemplate.convertAndSend(
                    "/topic/webrtc/user/candidate/" + participant.getUser().getUsername(),
                    message.getData()
            );
            log.debug("Sent ICE candidate: {}\nto user: {}", message.getData(), participant.getUser().getUsername());
        });
    }

    @Transactional
    public void handleOffer(SignalMessage message) {
        Chat chat = chatService.getChatById(message.getChatId());
        User user;

        try {
            user = userUtils.getByToken(message.getInitiatorToken());
        } catch (UserNotFound e) {
            log.warn(e.getMessage());
            throw new RuntimeException(e);
        }

        SignalOffer offer = new SignalOffer();
        offer.setChatId(chat.getId());
        offer.setInitiatorUsername(user.getUsername());
        offer.setData(message.getData());

        chat.getParticipants().forEach(participant -> {
            if (!participant.getUser().getUsername().equals(user.getUsername())) {
                simpMessagingTemplate.convertAndSend(
                        "/topic/webrtc/user/offer/" + participant.getUser().getUsername(),
                        offer
                );
                log.debug("Sent offer: {}\nto user: {}",offer, participant.getUser().getUsername());
            }
        });
    }

    @Transactional
    public void handleAnswer(SignalAnswer answer) {
        Chat chat = chatService.getChatById(answer.getChatId());
        User respondent;
        User initiator;

        try {
            respondent = userUtils.getByToken(answer.getRespondentToken());
            initiator = userUtils.getByUsername(answer.getInitiatorUsername());
        } catch (UserNotFound e) {
            log.warn(e.getMessage());
            throw new RuntimeException(e);
        }

        boolean respondentInChat = chat.getParticipants().stream().anyMatch(
                participant -> participant.getUser().equals(respondent)
        );

        boolean initiatorInChat = chat.getParticipants().stream().anyMatch(
                participant -> participant.getUser().equals(initiator)
        );

        if (respondentInChat && initiatorInChat) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/webrtc/user/answer/" + initiator.getUsername(),
                    answer.getData());
            log.debug("Sent answer: {}\nto user: {}", answer.getData(), initiator.getUsername());
        }
    }

    @Transactional
    public void handleCall(CallMessage message) {
        System.out.println(message);
        switch (message.getType()){
            case REQUEST -> {
                try {
                    User sender = userUtils.getByToken(message.getSenderToken());
                    User respondent = userUtils.getByUsername(message.getRespondentUsername());

                    CallAnswer callAnswer = new CallAnswer();
                    callAnswer.setChatId(message.getChatId());
                    callAnswer.setSenderUsername(sender.getUsername());

                    simpMessagingTemplate.convertAndSend(
                            "/topic/user/call/request/" + respondent.getUsername(),
                            callAnswer);
                } catch (UserNotFound e) {
                    log.warn(e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            case ACCEPT -> {
                try {
                    User respondent = userUtils.getByUsername(message.getRespondentUsername());

                    simpMessagingTemplate.convertAndSend(
                            "/topic/user/call/accept/" + respondent.getUsername(),
                            "ACCEPTED");
                } catch (UserNotFound e) {
                    log.warn(e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            case REJECT -> {
                try {
                    User respondent = userUtils.getByUsername(message.getRespondentUsername());

                    simpMessagingTemplate.convertAndSend(
                            "/topic/user/call/reject/" + respondent.getUsername(),
                            "REJECTED");
                } catch (UserNotFound e) {
                    log.warn(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
