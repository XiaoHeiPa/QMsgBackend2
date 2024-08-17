package org.cubewhy.chat.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.cubewhy.chat.conventer.PermissionConverter;

import java.util.Set;

@Data
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Convert(converter = PermissionConverter.class)
    private Set<Permission> permissions;

    @ManyToMany(mappedBy = "roles")
    private Set<Account> accounts;
}
