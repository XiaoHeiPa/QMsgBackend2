package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.Role;

import java.util.Set;

public interface RoleService {
    Role findByName(String name);

    Role createRole(String name, String description, Permission... permissions);

    Set<Role> findAll(Account account);

    Role findById(long id);
}
