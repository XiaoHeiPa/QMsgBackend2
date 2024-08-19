package org.cubewhy.chat.entity.vo;

import lombok.Data;

@Data
public class DownloadKeyVO {
    private String key;
    private long createAt;
    private long expireAt;
}
