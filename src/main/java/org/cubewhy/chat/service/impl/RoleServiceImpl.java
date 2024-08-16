package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.Role;
import org.cubewhy.chat.repository.RoleRepository;
import org.cubewhy.chat.service.RoleService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    @Resource
    RoleRepository roleRepository;

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public Role createRole(String name, Permission... permissions) {
        return roleRepository.save(Role.builder()
                .permissions(Arrays.stream(permissions).collect(Collectors.toSet()))
                .name(name)
                .build());
    }
}
