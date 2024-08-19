package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.dto.InviteCodeDTO;

import java.util.List;
import java.util.Set;

public interface AccountService {
    Account findAccountByNameOrEmail(String usernameOrEmail);

    List<Account> findAllAccounts();

    Account createAccount(Account account);

    void deleteAccountById(long id);

    Account findAccountById(long id);

    Account save(Account account);

    boolean checkPermission(Account account, Permission... permission);

    List<Channel> findManagedChannels(Account account);

    List<Channel> findJoinedChannels(Account account);

    Set<Role> useInviteCode(String code);

    InviteCode createInviteCode(String code, Role... roles);

    InviteCode createInviteCode(InviteCodeDTO dto);

    boolean existByUsername(String username);
}
