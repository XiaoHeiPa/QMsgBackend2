package org.cubewhy.chat.service;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Role;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Permission;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Resource
    AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountService.findAccountByNameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException(username);
        }
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRoles().stream().map(Role::getName).toArray(String[]::new))
                .authorities(account.getRoles().stream()
                        .map(Role::getPermissions)
                        .flatMap(Set::stream).map(Enum::name).toArray(String[]::new))
                .build();
    }
}
