package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.Role;
import org.cubewhy.chat.repository.RoleRepository;
import org.cubewhy.chat.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    @Resource
    RoleRepository roleRepository;

    @Override
    @Transactional
    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    @Transactional
    public Role createRole(String name, String description, Permission... permissions) {
        if (roleRepository.existsByName(name)) return roleRepository.findByName(name);
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(Arrays.stream(permissions).collect(Collectors.toSet()));
        return roleRepository.save(role);
    }

    @Override
    public Set<Role> findAll(Account account) {
        return roleRepository.findAllByAccounts_Username(account.getUsername());
    }
}
