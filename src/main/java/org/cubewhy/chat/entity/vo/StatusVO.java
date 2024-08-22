package org.cubewhy.chat.entity.vo;

import lombok.Data;

@Data
public class StatusVO {
    private long timestamp;
    private String impl = "cubewhy/QMsgBackend";
}
