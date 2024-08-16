package org.cubewhy.chat.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Message {
    private int sender;
    private int channel;
    private String content;
    private String timestamp;
}
