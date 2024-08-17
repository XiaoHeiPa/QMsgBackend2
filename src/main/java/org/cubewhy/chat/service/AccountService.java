package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Role;

public interface AccountService {
    Account findAccountByNameOrEmail(String usernameOrEmail);

    Account createAccount(String username, String rawPassword, Role... roles);
}
