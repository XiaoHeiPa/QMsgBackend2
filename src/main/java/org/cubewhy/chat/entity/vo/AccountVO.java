package org.cubewhy.chat.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class AccountVO {
    private long id;
    private String username;
    private String nickname;
    private String avatarHash;
    private String email;
    private String bio;
    private long registerTime;
    private long updatedTime;
    private List<String> roles;
}
