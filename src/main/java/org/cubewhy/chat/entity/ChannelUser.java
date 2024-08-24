package org.cubewhy.chat.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.cubewhy.chat.conventer.PermissionConverter;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
public class ChannelUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Account user;

    private String channelNickname;

    private LocalDateTime joinedAt; // Additional attribute

    @Convert(converter = PermissionConverter.class)
    private Set<Permission> permissions;

    @PrePersist
    protected void onCreate() {
        this.channelNickname = user.getNickname();
    }
}