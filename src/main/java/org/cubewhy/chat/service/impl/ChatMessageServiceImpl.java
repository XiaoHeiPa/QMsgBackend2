package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.ChatMessage;
import org.cubewhy.chat.entity.dto.ChatMessageDTO;
import org.cubewhy.chat.repository.ChatMessageRepository;
import org.cubewhy.chat.service.ChatMessageService;
import org.cubewhy.chat.util.KafkaConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Resource
    KafkaTemplate<String, ChatMessage> kafkaTemplate;

    @Override
    public ChatMessage saveMessage(ChatMessageDTO message, Account sender) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChannel(message.getChannel());
        chatMessage.setSender(sender.getId());
        chatMessage.setContentType(message.getContentType());
        chatMessage.setContent(message.getContent());
        ChatMessage saved = chatMessageRepository.save(chatMessage);
        kafkaTemplate.send(KafkaConstants.KAFKA_TOPIC, saved); // push to kafka
        return saved;
    }

    @Override
    public Page<ChatMessage> getMessagesByChannel(long channel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("id")));
        return chatMessageRepository.findByChannel(channel, pageable);
    }

    @Override
    public Page<ChatMessage> getMessagesBySenderAndChannel(long sender, long channel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findBySenderAndChannel(sender, channel, pageable);
    }

    @Override
    public void deleteAllByChannelId(Long channelId) {
        chatMessageRepository.deleteAllByChannel(channelId);
    }

    @Override
    public ChatMessage findMessageById(long messageId) {
        return chatMessageRepository.findById(messageId).orElse(null);
    }

    @Override
    public void deleteMessage(ChatMessage message) {
        chatMessageRepository.delete(message);
    }
}
