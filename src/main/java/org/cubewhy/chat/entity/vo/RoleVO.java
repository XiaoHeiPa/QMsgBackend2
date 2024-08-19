package org.cubewhy.chat.entity.vo;

import lombok.Data;
import org.cubewhy.chat.entity.Permission;

import java.util.Set;

@Data
public class RoleVO {
    private long id;
    private String name;
    private String description;
    private Set<Permission> permissions;
}
