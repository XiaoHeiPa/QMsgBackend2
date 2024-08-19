package org.cubewhy.chat.entity.vo;

import lombok.Data;

@Data
public class ChannelInviteCodeVO {
    private String code;
    private long channelId;
    private long createUser;

    private long expireAt;
    private boolean expireAfterUse;
}
