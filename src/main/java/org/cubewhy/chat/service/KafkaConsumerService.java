package org.cubewhy.chat.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.ChatMessage;
import org.cubewhy.chat.util.KafkaConstants;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

//@Service
public class KafkaConsumerService {
    @Resource
    PushService pushService;

    @KafkaListener(topics = KafkaConstants.KAFKA_TOPIC, groupId = KafkaConstants.GROUP_ID)
    public void listen(ChatMessage message) throws IOException, FirebaseMessagingException {
        pushService.push(message);
    }
}

