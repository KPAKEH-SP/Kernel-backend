package ru.lcp.kernel.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final UserUtils userUtils;

    @Transactional
    public String handleCandidate(SignalMessage message, String respondentUsername) {
        System.out.println(respondentUsername);
        try {
            User initiator = userUtils.getByToken(message.getSenderToken());

            return message.getData();
        } catch (UserNotFound e) {
            log.warn(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public String handleOffer(SignalMessage message, String respondentUsername) {
        try {
            User user = userUtils.getByToken(message.getSenderToken());

            return message.getData();
        } catch (UserNotFound e) {
            log.warn(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public String handleAnswer(SignalMessage message, String respondentUsername) {
        try {
            User sender = userUtils.getByToken(message.getSenderToken());

            return message.getData();
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
