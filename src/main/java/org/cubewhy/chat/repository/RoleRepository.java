package org.cubewhy.chat.repository;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
    boolean existsByName(String name);
    Set<Role> findAllByAccounts_Username(String accounts_username);
}
