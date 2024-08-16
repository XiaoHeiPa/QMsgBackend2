package org.cubewhy.chat.entity;

import jakarta.transaction.Transactional;
import lombok.Getter;
import org.hibernate.Hibernate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Getter
public class UserDetailsImpl implements UserDetails {
    private final Account account;

    public UserDetailsImpl(Account user) {
        this.account = user;
    }

    @Override
    @Transactional
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return account.getRoles().stream()
                .map(Role::getPermissions)
                .flatMap(Set::stream).map(permission -> new SimpleGrantedAuthority(permission.name())).toList();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getUsername();
    }
}
