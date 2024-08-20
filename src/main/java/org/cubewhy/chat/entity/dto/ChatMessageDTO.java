package org.cubewhy.chat.entity.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class ChatMessageDTO {
    private long channel;

    private String contentType;
    private JSONObject content;
}
