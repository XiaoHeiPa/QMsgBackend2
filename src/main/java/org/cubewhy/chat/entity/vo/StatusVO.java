package org.cubewhy.chat.entity.vo;

import lombok.Data;

@Data
public class StatusVO {
    private String serverName;
    private long timestamp;
    private String impl = "cubewhy/QMsgBackend";
    private MotdVO motd = null;
}
