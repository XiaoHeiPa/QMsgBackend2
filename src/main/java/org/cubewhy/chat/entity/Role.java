package org.cubewhy.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.cubewhy.chat.conventer.PermissionConverter;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Convert(converter = PermissionConverter.class)
    private Set<Permission> permissions;

    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<Account> accounts = new HashSet<>();
}
