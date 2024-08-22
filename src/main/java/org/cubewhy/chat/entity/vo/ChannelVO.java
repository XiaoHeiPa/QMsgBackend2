package org.cubewhy.chat.entity.vo;

import lombok.Data;

@Data
public class ChannelVO {
    private Long id;

    private String name;
    private String title;
    private String description;

    private String iconHash;
    private boolean publicChannel;
    private boolean decentralized;

    private long createdAt;
    private long memberCount;
}
