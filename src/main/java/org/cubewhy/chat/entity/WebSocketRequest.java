package org.cubewhy.chat.entity;

import lombok.Data;

@Data
public class WebSocketRequest<T> {
    private String method;
    private T data;
}
