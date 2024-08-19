package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Channel;
import org.cubewhy.chat.entity.Permission;
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

    boolean checkPermission(Account account, Permission... permission);

    List<Channel> findManagedChannels(Account account);

    List<Channel> findJoinedChannels(Account account);
}
