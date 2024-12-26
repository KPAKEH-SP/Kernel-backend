package ru.lcp.kernel.utils;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import ru.lcp.kernel.entities.User;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WsSendMessageUtil {
    private final UsersConnections usersConnections;

    public void sendMessageToUser(User user, WsMessage<?> message) throws IOException {
        List<WebSocketSession> userSessions = usersConnections.getConnections(user);

        Gson gson = new Gson();
        String jsonMessage = gson.toJson(message);

        for (WebSocketSession userSession : userSessions) {
            if (userSession.isOpen()) {
                userSession.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }
}
