package org.cubewhy.chat.config;

import jakarta.annotation.Resource;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.util.JwtUtil;
import org.cubewhy.chat.websocket.ChatHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer {
    @Resource
    JwtUtil jwtUtil;

    @Resource
    AccountService accountService;

    @Resource
    ChannelService channelService;

    @Resource
    ChatHandler chatHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/websocket")
                .setAllowedOriginPatterns("*");
    }
}
