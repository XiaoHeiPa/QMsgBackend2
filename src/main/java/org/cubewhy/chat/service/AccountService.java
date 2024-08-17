package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;

public interface AccountService {
    Account findAccountByNameOrEmail(String usernameOrEmail);

    Account createAccount(String username, String rawPassword, String... roleNames);
}
