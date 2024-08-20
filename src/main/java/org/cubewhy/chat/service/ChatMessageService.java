package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.ChatMessage;
import org.cubewhy.chat.entity.dto.ChatMessageDTO;
import org.springframework.data.domain.Page;

public interface ChatMessageService {
    ChatMessage saveMessage(ChatMessageDTO message, Account sender);
    Page<ChatMessage> getMessagesByChannel(long channel, int page, int size);

    Page<ChatMessage> getMessagesBySenderAndChannel(long sender, long channel, int page, int size);

    void deleteAllByChannelId(Long channelId);

    ChatMessage getMessageById(long messageId);
}
