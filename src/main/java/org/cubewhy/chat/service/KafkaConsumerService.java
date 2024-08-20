package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.ChatMessage;
import org.cubewhy.chat.util.KafkaConstants;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = KafkaConstants.KAFKA_TOPIC, groupId = KafkaConstants.GROUP_ID)
    public void listen(ChatMessage message) {
        // todo
    }
}

