package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.ChatMessage;
import org.cubewhy.chat.entity.dto.ChatMessageDTO;
import org.cubewhy.chat.repository.ChatMessageRepository;
import org.cubewhy.chat.service.ChatMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {
    @Resource
    private ChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessage saveMessage(ChatMessageDTO message, long channelId, Account sender) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChannel(channelId);
        chatMessage.setSender(sender.getId());
        chatMessage.setContent(message.getContent());
        chatMessage.setTimestamp(System.currentTimeMillis());
        return chatMessageRepository.save(chatMessage);
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
}
