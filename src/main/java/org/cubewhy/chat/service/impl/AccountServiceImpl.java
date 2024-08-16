package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.Role;
import org.cubewhy.chat.repository.AccountRepository;
import org.cubewhy.chat.repository.RoleRepository;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.RoleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {
    @Resource
    AccountRepository accountRepository;
    @Resource
    @Lazy
    PasswordEncoder passwordEncoder;
    @Resource
    RoleService roleService;

    @Override
    public Account findAccountByNameOrEmail(String usernameOrEmail) {
        return accountRepository.findByUsername(usernameOrEmail).orElse(accountRepository.findByEmail(usernameOrEmail).orElse(null));
    }

    @Transactional
    @Override
    public Account createAccount(String username, String rawPassword, Set<String> roleNames) {
        // Check if the username already exists
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Create a new account
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(rawPassword));

        // Fetch roles from the database
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleService.findByName(roleName);
            if (role == null) {
                roleService.createRole(roleName, Permission.SEND_MESSAGE, Permission.JOIN_CHANNEL, Permission.CREATE_CHANNEL);
            }
            roles.add(role);
        }
        account.setRoles(roles);

        // Save the account to the database
        return accountRepository.save(account);
    }
}
