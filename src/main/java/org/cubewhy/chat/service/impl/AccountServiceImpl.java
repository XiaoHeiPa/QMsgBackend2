package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Role;
import org.cubewhy.chat.repository.AccountRepository;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.RoleService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class AccountServiceImpl implements AccountService {
    @Resource
    AccountRepository accountRepository;
    @Resource
    @Lazy
    PasswordEncoder passwordEncoder;
    @Resource
    RoleService roleService;
    @Resource
    ChannelService channelService;

    @Override
    @Transactional
    public Account findAccountByNameOrEmail(String usernameOrEmail) {
        Account account = accountRepository.findByUsername(usernameOrEmail).orElse(accountRepository.findByEmail(usernameOrEmail).orElse(null));
        if (account == null) return null;
        account.setRoles(roleService.findAll(account));
        account.setChannelUsers(channelService.findChannelUsers(account));
        return account;
    }

    @Override
    @Transactional
    public Account findAccountById(long id) {
        Optional<Account> accountOptional = accountRepository.findById(id);
        if (accountOptional.isEmpty()) return null;
        Account account = accountOptional.get();
        account.setRoles(roleService.findAll(account));
        account.setChannelUsers(channelService.findChannelUsers(account));
        return account;
    }

    @Transactional
    @Override
    public Account createAccount(String username, String rawPassword, Role... roles1) {
        // Check if the username already exists
        Optional<Account> existAccount = accountRepository.findByUsername(username);
        if (existAccount.isPresent()) return existAccount.get();

        // Create a new account
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(rawPassword));

        // Fetch roles from the database
        Set<Role> roles = new HashSet<>();
        account.setRoles(roles);

        // Save the account to the database
        return accountRepository.save(account);
    }

    @Override
    public Account createAccount(Account account) {
        Optional<Account> existAccount = accountRepository.findById(account.getId());
        return existAccount.orElseGet(() -> accountRepository.save(account));
    }

    @Override
    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public void deleteAccountById(long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public Account save(Account account) {
        return accountRepository.save(account);
    }
}
