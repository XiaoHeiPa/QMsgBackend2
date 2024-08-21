package org.cubewhy.chat.websocket;

import com.alibaba.fastjson2.JSON;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.ChatMessage;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.WebSocketRequest;
import org.cubewhy.chat.entity.dto.ChatMessageDTO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.ChatMessageService;
import org.cubewhy.chat.service.SessionService;
import org.cubewhy.chat.util.JwtUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Log4j2
@Component
public class ChatHandler extends TextWebSocketHandler {
    @Resource
    SessionService sessionService;

    @Resource
    AccountService accountService;

    @Resource
    ChatMessageService chatMessageService;

    @Resource
    ChannelService channelService;

    @Resource
    JwtUtil jwtUtil;

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        DecodedJWT jwt = jwtUtil.resolveJwt(session.getHandshakeHeaders().getFirst("Authorization"));
        Account account = accountService.findAccountById(jwtUtil.getId(jwt));
        log.info("User {} has connected to the chat server", account.getUsername());
        sessionService.addSession(account.getId(), session);
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) throws Exception {
        WebSocketRequest request = JSON.parseObject(message.getPayload(), WebSocketRequest.class);
        Account user = sessionService.getUser(session);
        if (request.getMethod().equals(WebSocketRequest.SEND_MESSAGE)) {
            ChatMessageDTO data = JSON.parseObject(request.getData().toJSONString(), ChatMessageDTO.class);
            if (channelService.checkPermissions(user, channelService.findChannelById(data.getChannel()), Permission.SEND_MESSAGE)) {
                chatMessageService.saveMessage(data, user);
            }
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        sessionService.removeSession(session);
    }
}
