package org.cubewhy.chat.entity.vo;

import lombok.Data;
import org.cubewhy.chat.entity.Role;

import java.util.Set;

@Data
public class AuthorizeVO {
    String username;
    String token;
    String email;
    Set<Role> roles;
    long expire;
}
