package org.cubewhy.chat.service;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Message;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    @Resource
    KafkaTemplate<String, Message> kafkaTemplate;


    public void sendMessage(String topic, Message message) {
        kafkaTemplate.send(topic, message);
    }
}
