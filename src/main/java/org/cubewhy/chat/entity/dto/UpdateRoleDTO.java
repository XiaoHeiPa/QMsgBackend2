package org.cubewhy.chat.entity.dto;

import lombok.Data;

@Data
public class UpdateRoleDTO {
    private long accountId;
    private long[] roles;
}
