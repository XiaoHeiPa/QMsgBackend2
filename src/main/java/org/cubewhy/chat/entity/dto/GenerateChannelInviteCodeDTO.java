package org.cubewhy.chat.entity.dto;

import lombok.Data;

@Data
public class GenerateChannelInviteCodeDTO {
    private long channelId;
    private String code;

    private long expireAt;
    private boolean expireAfterUse;
}
