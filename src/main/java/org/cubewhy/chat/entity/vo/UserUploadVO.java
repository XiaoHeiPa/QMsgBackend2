package org.cubewhy.chat.entity.vo;

import lombok.Data;

@Data
public class UserUploadVO {
    private long id;

    private String hash;
    private String name;
    private String description;
    private long timestamp;
    private long uploadUser;
}
