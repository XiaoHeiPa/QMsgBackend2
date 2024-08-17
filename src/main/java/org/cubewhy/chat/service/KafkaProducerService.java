package org.cubewhy.chat.service;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.ChatMessage;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    @Resource
    KafkaTemplate<String, ChatMessage> kafkaTemplate;


    public void sendMessage(String topic, ChatMessage message) {
        kafkaTemplate.send(topic, message);
    }
}
