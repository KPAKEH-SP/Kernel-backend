package ru.lcp.kernel.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.lcp.kernel.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UsersConnections {
    Map<User, List<WebSocketSession>> connections = new HashMap<>();

    public void addConnection(User user, WebSocketSession session) {
        if (connections.containsKey(user)) {
            connections.get(user).add(session);
        } else {
            List<WebSocketSession> sessions = new ArrayList<>();
            sessions.add(session);

            connections.put(user, sessions);
        }
    }

    public void removeConnection(User user) {
        connections.remove(user);
    }
}
