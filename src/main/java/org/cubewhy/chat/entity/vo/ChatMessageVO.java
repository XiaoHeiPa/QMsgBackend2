package org.cubewhy.chat.entity.vo;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageVO {
    private long id;

    private ChannelVO channel;
    private SenderVO sender;
    private String shortContent;
    private String contentType;
    private List<JSONObject> content;
    private long timestamp;
}
