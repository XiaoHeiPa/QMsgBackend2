package org.cubewhy.chat.entity;

import com.alibaba.fastjson2.JSONObject;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.cubewhy.chat.conventer.MessageContentConverter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
@Entity
public class ChatMessage implements BaseData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long channel;
    private long sender;
    private String shortContent; // 通知/预览显示内容,由客户端决定
    @Convert(converter = MessageContentConverter.class)
    @Column(columnDefinition = "LONGTEXT")
    private List<JSONObject> content;
    private LocalDateTime timestamp;
    private LocalDateTime editTimestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
        this.editTimestamp = timestamp;
    }

    @PreUpdate
    protected void onUpdate() {
        this.editTimestamp = LocalDateTime.now();
    }
}
