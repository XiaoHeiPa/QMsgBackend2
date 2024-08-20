package org.cubewhy.chat.entity;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class WebSocketRequest {
    public static final String SEND_MESSAGE = "smsg";

    private String method;
    private JSONObject data;
}
