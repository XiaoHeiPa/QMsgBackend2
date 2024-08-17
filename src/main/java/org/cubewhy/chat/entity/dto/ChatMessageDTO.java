package org.cubewhy.chat.entity.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class ChatMessageDTO {
    private String contentType;
    private JSONObject content;
}
