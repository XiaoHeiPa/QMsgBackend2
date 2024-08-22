package org.cubewhy.chat.entity.vo;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageVO {
    private long id;

    private ChannelVO channel;
    private long sender;
    private String shortContent;
    private String contentType;
    private JSONObject content;
    private long timestamp;
}
