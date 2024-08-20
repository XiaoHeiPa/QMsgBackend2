package org.cubewhy.chat.config;

import jakarta.annotation.Resource;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.util.JwtUtil;
import org.cubewhy.chat.websocket.ChatHandler;
import org.springframework.context.annotation.Bean;
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

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler(), "/chat/websocket")
                .setAllowedOrigins("*");
    }

    @Bean
    public ChatHandler chatHandler() {
        return new ChatHandler();
    }

    //    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        registration.interceptors(new ChannelInterceptor() {
//            @Override
//            public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
//                StompHeaderAccessor accessor =
//                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//                assert accessor != null;
//                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                    String token = Objects.requireNonNull(accessor.getNativeHeader("Authorization")).get(0);
//                    DecodedJWT jwt = jwtUtil.resolveJwt(token);
//                    if (jwt == null || jwtUtil.isInvalidToken(jwt.getId())) return null;
//                    UserDetails user = jwtUtil.toUser(jwt);
//                    Account account = accountService.findAccountByNameOrEmail(user.getUsername());
//                    if (account != null) {
//                        accessor.setUser(account);
//                        return message;
//                    }
//                    return null;
//                }
//                return message;
//            }
//
//            @Override
//            public void afterSendCompletion(@NotNull Message<?> message, @NotNull MessageChannel channel, boolean sent, Exception ex) {
//                String destination = message.getHeaders().get("simpDestination", String.class);
//                StompHeaderAccessor accessor =
//                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//                Account account = (Account) Objects.requireNonNull(accessor).getUser();
//                if (destination != null && destination.startsWith("/topic/channel") && !channelService.hasViewPermission(account, Long.parseLong(destination.split("/")[2]))) {
//                    throw new IllegalArgumentException("You have no permission to view this channel");
//                }
//                ChannelInterceptor.super.afterSendCompletion(message, channel, sent, ex);
//            }
//        });
//    }
}
