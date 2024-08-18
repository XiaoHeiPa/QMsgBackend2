package org.cubewhy.chat.entity.dto;

import lombok.Data;

@Data
public class JoinRequestDTO {
    private Long channelId;
    private String reason;
}
