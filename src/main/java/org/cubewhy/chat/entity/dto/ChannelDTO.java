package org.cubewhy.chat.entity.dto;

import lombok.Data;

@Data
public class ChannelDTO {
    private String name; // Used for invite links
    private String title;
    private String description;
    private String iconHash;
}
