package org.cubewhy.chat.entity;

import lombok.Data;

@Data
public class WebSocketResponse<T> {
    private String method;
    private T data;
}
