package org.cubewhy.chat.entity;

import jakarta.persistence.*;
import lombok.Data;

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


    @OneToMany(mappedBy = "channel")
    private List<ChannelUser> channelUsers;
}
