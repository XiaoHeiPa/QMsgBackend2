package org.cubewhy.chat.entity;

import lombok.Data;

import java.util.Set;

@Data
public class ChannelConfInfo {
    private String nickname;
    private Set<Permission> permissions;
}
