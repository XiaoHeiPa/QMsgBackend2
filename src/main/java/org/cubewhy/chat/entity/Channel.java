package org.cubewhy.chat.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // used for invite links
    private String title;
    private String description;

    private String iconHash;
    private boolean publicChannel = false;

    // 去中心化
    // 开启后所有人退出群组自动解散,不能手动解散
    // 没有任何人有特殊权限 (除服务器管理员)
    private boolean decentralized = false;

    @OneToMany(mappedBy = "channel")
    private List<ChannelUser> channelUsers;

    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
    }
}
