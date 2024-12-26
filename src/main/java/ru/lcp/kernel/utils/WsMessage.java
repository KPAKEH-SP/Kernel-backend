package ru.lcp.kernel.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Data;
import ru.lcp.kernel.enums.WebSocketMessagesTypes;

@Data
public class WsMessage<T> {
    private WebSocketMessagesTypes type;
    private T data;

    public WsMessage(WebSocketMessagesTypes type, T data) {
        this.type = type;
        this.data = data;
    }

    public static WsMessage<?> fromJson(String json, Class<?> dataType) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        WebSocketMessagesTypes type = gson.fromJson(jsonObject.get("type"), WebSocketMessagesTypes.class);
        Object data = gson.fromJson(jsonObject.get("data"), dataType);
        return new WsMessage<>(type, data);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
