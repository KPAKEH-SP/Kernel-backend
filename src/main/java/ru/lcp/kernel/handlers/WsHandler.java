package ru.lcp.kernel.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import ru.lcp.kernel.dtos.Token;
import ru.lcp.kernel.dtos.webSocketV2.FriendPayload;
import ru.lcp.kernel.utils.WsMessage;
import ru.lcp.kernel.enums.WebSocketMessagesTypes;

@Component
@RequiredArgsConstructor
public class WsHandler implements WebSocketHandler {

    private final WsEventHandler wsEventHandler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage textMessage) {
            String payload = textMessage.getPayload();
            Class<?> dataType = determineDataType(payload);
            WsMessage<?> wsMessage = WsMessage.fromJson(payload, dataType);
            wsEventHandler.handleMessage(session, wsMessage);
        }
    }

    private Class<?> determineDataType(String payload) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(payload, JsonObject.class);
        String type = jsonObject.get("type").getAsString();

        return switch (WebSocketMessagesTypes.valueOf(type)) {
            case CONNECT -> Token.class;
            case NEW_FRIEND_REQUEST -> FriendPayload.class;
        };
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
}
