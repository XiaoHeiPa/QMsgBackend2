package org.cubewhy.chat.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class UserUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String hash;
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Account uploadUser;
}
