package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.ChatMessage;

public interface PushService {
    void push(ChatMessage message);
}
