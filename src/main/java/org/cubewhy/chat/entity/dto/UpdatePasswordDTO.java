package org.cubewhy.chat.entity.dto;

import lombok.Data;

@Data
public class UpdatePasswordDTO {
    private long accountId;
    private String password;
}
