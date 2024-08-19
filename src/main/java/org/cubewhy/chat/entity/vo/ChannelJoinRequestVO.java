package org.cubewhy.chat.entity.vo;

import lombok.Data;

@Data
public class ChannelJoinRequestVO {
    private long id;
    private long channelId;
    private long userId;
    private String reason;
}
