package org.cubewhy.chat.entity.vo;

import lombok.Data;
import org.cubewhy.chat.entity.Role;

import java.util.Set;

@Data
public class AuthorizeVO {
    private String username;
    private String token;
    private String email;
    private Set<RoleVO> roles;
    private long expire;
}
