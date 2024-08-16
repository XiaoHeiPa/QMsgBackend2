package org.cubewhy.chat.entity.vo;

import lombok.Data;
import org.cubewhy.chat.entity.Role;

@Data
public class AuthorizeVO {
    String username;
    String token;
    String email;
    Role role;
    long expire;
}
