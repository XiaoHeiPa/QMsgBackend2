package org.cubewhy.chat.entity.dto;

import lombok.Data;

@Data
public class FriendRequestDTO {
    private long to;
    private String reason;
}
