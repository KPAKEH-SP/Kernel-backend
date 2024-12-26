package ru.lcp.kernel.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.lcp.kernel.dtos.Token;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.exceptions.UserNotFound;
import ru.lcp.kernel.utils.UserUtils;
import ru.lcp.kernel.utils.UsersConnections;
import ru.lcp.kernel.utils.WsMessage;

@Component
@RequiredArgsConstructor
public class WsEventHandler {
    private final UserUtils userUtils;
    private final UsersConnections usersConnections;

    public void handleMessage(WebSocketSession session, WsMessage<?> message) throws UserNotFound {
        switch (message.getType()) {
            case CONNECT -> {
                Token token = (Token) message.getData();

                User user = userUtils.getByToken(token.getToken());
                usersConnections.addConnection(user, session);
            }
        }
    }
}
