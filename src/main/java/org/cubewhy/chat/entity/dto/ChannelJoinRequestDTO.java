package org.cubewhy.chat.entity.dto;

import lombok.Data;

@Data
public class ChannelJoinRequestDTO {
    private Long channelId;
    private String reason;
}
