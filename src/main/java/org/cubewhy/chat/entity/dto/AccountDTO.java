package org.cubewhy.chat.entity.dto;

import lombok.Data;

@Data
public class AccountDTO {
    private String nickname;
    private String email;
    private String bio;

    private String username;
    private String password;
    private long[] roles;
}
