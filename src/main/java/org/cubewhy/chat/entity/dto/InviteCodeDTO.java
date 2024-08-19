package org.cubewhy.chat.entity.dto;

import lombok.Data;

import java.util.Set;

@Data
public class InviteCodeDTO {
    private String code;
    private Set<String> roles;
}
