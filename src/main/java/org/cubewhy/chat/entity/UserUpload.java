package org.cubewhy.chat.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class UserUpload implements BaseData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String hash;
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Account uploadUser;

    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
