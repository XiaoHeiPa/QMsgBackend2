package org.cubewhy.chat.entity;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebSocketResponse<T> {
    public static final String NEW_MESSAGE = "nmsg";

    private String method;
    private T data;

    public String toJson() {
        return JSON.toJSONString(this);
    }
}
