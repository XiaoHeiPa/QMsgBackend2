package org.cubewhy.chat.entity.dto;

import lombok.Data;

@Data
public class GenerateChannelInviteCodeDTO {
    private long channelId;
    private long createUser;
    private String code;

    private long expireAt;
    private boolean expireAfterUse;
}
