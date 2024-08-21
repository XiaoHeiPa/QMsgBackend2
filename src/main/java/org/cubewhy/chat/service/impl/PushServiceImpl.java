package org.cubewhy.chat.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.PushService;
import org.cubewhy.chat.service.SessionService;
import org.cubewhy.chat.util.RedisConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Log4j2
@Service
public class PushServiceImpl implements PushService {
    @Resource
    RedisTemplate<String, String> redisTemplate;

    @Resource
    FirebaseMessaging firebaseMessaging;

    @Resource
    ChannelService channelService;

    @Resource
    SessionService sessionService;

    @Value("${spring.application.push.fcm.state}")
    private boolean fcmState;

    @Override
    public void push(ChatMessage message) throws FirebaseMessagingException, IOException {
        Channel channel = channelService.findChannelById(message.getChannel());
        for (ChannelUser user : channel.getChannelUsers()) {
            Account account = user.getUser();
            if (fcmState) {
                String token = getToken(account.getId());
                if (token != null) {
                    Message fcmMessage = Message.builder()
                            .setToken(token)
                            .setNotification(Notification.builder()
                                    .setTitle(channel.getTitle())
                                    .setBody(message.getShortContent())
                                    .build())
                            .build();
                    firebaseMessaging.send(fcmMessage);
                }
            }
            // push via websockets
            WebSocketSession session = sessionService.getSession(account.getId());
            if (session != null) {
                session.sendMessage(new TextMessage(new WebSocketResponse<>(WebSocketResponse.NEW_MESSAGE, message).toJson()));
            }
        }
    }

    @Override
    public String getToken(long accountId) {
        return redisTemplate.opsForValue().get(RedisConstants.FCM_TOKEN + accountId);
    }

    @Override
    public void updateFirebaseToken(Account account, String token) {
        redisTemplate.opsForValue().set(RedisConstants.FCM_TOKEN + account.getId(), token);
    }
}
