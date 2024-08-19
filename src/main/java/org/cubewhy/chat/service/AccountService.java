package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Role;

import java.util.List;

public interface AccountService {
    Account findAccountByNameOrEmail(String usernameOrEmail);

    Account createAccount(String username, String rawPassword, Role... roles);

    List<Account> findAllAccounts();

    Account createAccount(Account account);

    void deleteAccountById(long id);

    Account findAccountById(long id);

    Account save(Account account);
}
