package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.SessionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SessionServiceImpl implements SessionService {
    @Resource
    AccountService accountService;

    private final ConcurrentHashMap<WebSocketSession, Long> sessions = new ConcurrentHashMap<>();

    @Override
    public void addSession(Long user, WebSocketSession session) {
        sessions.put(session, user);
    }

    @Override
    public void removeSession(Long user) {
        sessions.forEach((key, value) -> {
            if (value.equals(user)) {
                sessions.remove(key);
            }
        });
    }

    @Override
    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    @Override
    public WebSocketSession getSession(Long user) {
        for (Map.Entry<WebSocketSession, Long> entry : sessions.entrySet()) {
            if (entry.getValue().equals(user)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public Account getUser(@NotNull WebSocketSession session) {
        AtomicReference<Account> result = new AtomicReference<>();
        sessions.forEach((key, value) -> {
            if (value.equals(session)) {
                result.set(accountService.findAccountById(key));
            }
        });
        return result.get();
    }
}
