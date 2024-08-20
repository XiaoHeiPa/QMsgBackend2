package org.cubewhy.chat.service;

import org.springframework.web.socket.WebSocketSession;

public interface SessionService {
    void addSession(Long user, WebSocketSession session);

    void removeSession(Long user);

    void removeSession(WebSocketSession session);

    WebSocketSession getSession(Long user);

}
