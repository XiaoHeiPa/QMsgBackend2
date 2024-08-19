package org.cubewhy.chat.entity.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String email;

    private String nickname;
    private String bio;

    private String inviteCode; // optional
}
