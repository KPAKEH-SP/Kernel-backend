package ru.lcp.kernel.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import ru.lcp.kernel.handlers.WsHandler;
import ru.lcp.kernel.utils.UserUtils;
import ru.lcp.kernel.utils.UsersConnections;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfigV2 implements WebSocketConfigurer {
    private final UserUtils userUtils;
    private final UsersConnections usersConnections;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WsHandler(userUtils, usersConnections), "/ws-v2").setAllowedOrigins("https://kernel-frontend-one.vercel.app");
    }
}
