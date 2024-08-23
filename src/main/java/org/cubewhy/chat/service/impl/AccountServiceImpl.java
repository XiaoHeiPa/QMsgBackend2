package org.cubewhy.chat.service.impl;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.dto.InviteCodeDTO;
import org.cubewhy.chat.repository.AccountRepository;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.RoleService;
import org.cubewhy.chat.util.RedisConstants;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
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

    @Resource
    RedisTemplate<String, InviteCode> inviteCodeRedisTemplate;

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

    @Override
    public Account createAccount(Account account) {
        Optional<Account> existAccount;
        if (account.getId() != null) {
            existAccount = accountRepository.findById(account.getId());
        } else {
            existAccount = accountRepository.findByUsername(account.getUsername());
        }
        if (existAccount.isPresent()) return existAccount.get();
        if (channelService.hasName(account.getName())) {
            return null;
        }
        log.info("Account {} was created", account.getUsername());
        return accountRepository.save(account);
    }

    @Override
    public List<Account> findAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public void deleteAccountById(long id) {
        log.info("Account with id {} was deleted", id);
        accountRepository.deleteById(id);
    }

    @Override
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public boolean checkPermission(Account account, Permission... permission) {
        return account.getPermissions().containsAll(Arrays.asList(permission));
    }

    @Override
    @Transactional
    public List<Channel> findManagedChannels(Account account) {
        return account.getChannelUsers().stream()
                .filter(cu ->
                        cu.getPermissions().contains(Permission.MANAGE_CHANNEL))
                .map(ChannelUser::getChannel).toList();
    }

    @Override
    @Transactional
    public List<Channel> findJoinedChannels(Account account) {
        return channelService.findChannelUsers(account).stream().map(ChannelUser::getChannel).toList();
    }

    @Override
    public Set<Role> useInviteCode(String code) {
        InviteCode codeObj = inviteCodeRedisTemplate.opsForValue().getAndDelete(RedisConstants.INVITATION + code);
        if (codeObj == null) return Set.of();
        return codeObj.getRoles().stream().map(role -> roleService.findByName(role)).collect(Collectors.toSet());
    }

    @Override
    public InviteCode createInviteCode(String code, Role... roles) {
        InviteCode codeObj = new InviteCode();
        codeObj.setCode(code);
        codeObj.setRoles(Arrays.stream(roles).map(Role::getName).collect(Collectors.toSet()));
        saveInviteCode(codeObj);
        return codeObj;
    }

    @Override
    public InviteCode createInviteCode(InviteCodeDTO dto) {
        InviteCode codeObj = new InviteCode();
        codeObj.setCode(dto.getCode());
        codeObj.setRoles(dto.getRoles());
        saveInviteCode(codeObj);
        return codeObj;
    }

    @Override
    public boolean existByUsername(String username) {
        return accountRepository.existsByUsername(username);
    }

    @Override
    public Account findAccountByName(String username) {
        return accountRepository.findByUsername(username).orElse(null
        );
    }

    @Override
    @Transactional
    public Account findAccountByRequest(HttpServletRequest request) {
        return findAccountById((int) request.getAttribute("id"));
    }

    @Override
    public Account update(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account findAccountByIdNoExtra(long id) {
        return accountRepository.findById(id).orElse(null);
    }

    private void saveInviteCode(InviteCode codeObj) {
        inviteCodeRedisTemplate.opsForValue().set(RedisConstants.INVITATION + codeObj.getCode(), codeObj);
        log.info("Invite code {} was created", codeObj.getCode());
    }
}
