package org.cubewhy.chat.config;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.util.JwtUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Objects;

@EnableWebSocket
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Resource
    JwtUtil jwtUtil;
    @Resource
    AccountService accountService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // chat client will use this to connect to the server
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic/");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                assert accessor != null;
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = Objects.requireNonNull(accessor.getNativeHeader("Authorization")).get(0);
                    DecodedJWT jwt = jwtUtil.resolveJwt(token);
                    if (jwt == null || jwtUtil.isInvalidToken(jwt.getId())) return null;
                    UserDetails user = jwtUtil.toUser(jwt);
                    Account account = accountService.findAccountByNameOrEmail(user.getUsername());
                    if (account != null) {
                        accessor.setUser(account);
                        return message;
                    }
                    return null;
                }
                return message;
            }
        });
    }
}
