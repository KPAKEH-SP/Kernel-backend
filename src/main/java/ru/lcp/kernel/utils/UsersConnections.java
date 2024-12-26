package ru.lcp.kernel.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.lcp.kernel.entities.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UsersConnections {
    Map<User, List<WebSocketSession>> connections = new HashMap<>();

    public void addConnection(User user, WebSocketSession session) {
        connections.get(user).add(session);

        log.info("User {} connected", user);
    }

    public void removeConnection(User user) {
        connections.remove(user);
    }
}
