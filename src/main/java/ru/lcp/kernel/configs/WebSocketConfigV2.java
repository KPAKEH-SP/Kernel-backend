package ru.lcp.kernel.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.lcp.kernel.handlers.WsHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfigV2 implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WsHandler(), "/ws-v2").setAllowedOrigins("https://kernel-frontend-one.vercel.app", "http://localhost:5173");
    }
}
