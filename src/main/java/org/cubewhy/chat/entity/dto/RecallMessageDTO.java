package org.cubewhy.chat.entity.dto;

import lombok.Data;

import java.util.Set;

@Data
public class RecallMessageDTO {
    private Set<Long> messageId; // 可以根据message id反推发送人和频道,所以不需要用户提供.
}
