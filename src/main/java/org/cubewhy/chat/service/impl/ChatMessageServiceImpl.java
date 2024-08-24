package org.cubewhy.chat.service.impl;

import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.ChatMessage;
import org.cubewhy.chat.entity.dto.ChatMessageDTO;
import org.cubewhy.chat.repository.ChatMessageRepository;
import org.cubewhy.chat.service.ChatMessageService;
import org.cubewhy.chat.service.PushService;
import org.cubewhy.chat.util.KafkaConstants;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Log4j2
public class ChatMessageServiceImpl implements ChatMessageService {
    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Resource
    @Lazy
    PushService pushService;

    @Override
    public ChatMessage saveMessage(ChatMessageDTO message, Account sender) throws IOException, FirebaseMessagingException {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChannel(message.getChannel());
        chatMessage.setSender(sender.getId());
        chatMessage.setContent(message.getContent());
        chatMessage.setShortContent(calcShortContent(message.getShortContent()));
        ChatMessage saved = chatMessageRepository.save(chatMessage);
        pushService.push(saved);
//        log.info("Message from {}: {}", sender.getNickname(), chatMessage.getShortContent());
        return saved;
    }

    private String calcShortContent(String content) {
        if (content.length() > 10) {
            return content.substring(0, 10) + "...";
        }
        return content;
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
