package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Message;
import org.cubewhy.chat.service.KafkaProducerService;
import org.cubewhy.chat.util.KafkaConstants;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {
    @Resource
    KafkaProducerService kafkaProducerService;

    @MessageMapping("/sendMessage/")
    @SendTo("/topic/channel")
    public Message broadcastChannelMessage(@Payload Message message) {
        kafkaProducerService.sendMessage(KafkaConstants.KAFKA_TOPIC, message);
        return message;
    }
}
