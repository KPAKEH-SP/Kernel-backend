package ru.lcp.kernel.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.lcp.kernel.entities.User;
import ru.lcp.kernel.utils.UserUtils;
import ru.lcp.kernel.utils.UsersConnections;

@Component
@RequiredArgsConstructor
public class WsHandler implements WebSocketHandler {
    private final UserUtils userUtils;
    private final UsersConnections usersConnections;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = getTokenFromSession(session);
        if (token == null || token.isEmpty()) {
            System.out.println("Token is missing");
            session.close();
        } else {
            User user = userUtils.getByToken(token);
            usersConnections.addConnection(user, session);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        System.out.println(message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.out.println(exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        System.out.println(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String getTokenFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }
        return null;
    }
}
