package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Role;

import java.util.Set;

public interface AccountService {
    Account findAccountByNameOrEmail(String usernameOrEmail);
    Account createAccount(String username, String rawPassword, Set<String> roleNames);
}
