package org.cubewhy.chat.entity;

import lombok.Data;

import java.util.Set;

@Data
public class InviteCode {
    private String code;
    private Set<String> roles;
}
