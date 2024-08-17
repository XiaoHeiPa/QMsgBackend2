package org.cubewhy.chat.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelDTO {
    private String name; // Used for invite links
    private String title;
    private String description;
    private String iconHash;
}
