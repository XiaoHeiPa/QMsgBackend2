package org.cubewhy.chat.entity.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class ChatMessageDTO {
    private long channel;

    private String shortContent;
    private List<JSONObject> content;
}
