package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.ChatMessage;
import org.cubewhy.chat.entity.dto.ChatMessageDTO;

public interface ChatMessageService {
    ChatMessage saveMessage(ChatMessageDTO message, long channelId, Account sender);
}
