package org.cubewhy.chat.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DownloadKey implements BaseData {
    private String key;
    private LocalDateTime createAt;
    private LocalDateTime expireAt;
}
