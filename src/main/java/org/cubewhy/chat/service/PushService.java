package org.cubewhy.chat.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.ChatMessage;

import java.io.IOException;

public interface PushService {
    void push(ChatMessage message) throws FirebaseMessagingException, IOException;

    String getToken(long accountId);

    void updateFirebaseToken(Account account, String token);
}
