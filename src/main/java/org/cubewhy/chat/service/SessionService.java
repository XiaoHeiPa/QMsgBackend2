package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.util.JwtUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.WebSocketSession;

public interface SessionService {
    void addSession(Long user, WebSocketSession session);

    void removeSession(Long user);

    void removeSession(WebSocketSession session);

    WebSocketSession getSession(Long user);

    Account getUser(@NotNull WebSocketSession session);
}
