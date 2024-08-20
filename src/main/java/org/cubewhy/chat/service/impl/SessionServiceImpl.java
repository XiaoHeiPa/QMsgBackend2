package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.SessionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SessionServiceImpl implements SessionService {
    @Resource
    AccountService accountService;

    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void addSession(Long user, WebSocketSession session) {
        sessions.put(user, session);
    }

    @Override
    public void removeSession(Long user) {
        sessions.remove(user);
    }

    @Override
    public void removeSession(WebSocketSession session) {
        sessions.forEach((key, value) -> {
            if (value.equals(session)) {
                sessions.remove(key);
            }
        });
    }

    @Override
    public WebSocketSession getSession(Long user) {
        return sessions.get(user);
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
