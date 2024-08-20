package org.cubewhy.chat.service.impl;

import org.cubewhy.chat.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionServiceImpl implements SessionService {
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
}
