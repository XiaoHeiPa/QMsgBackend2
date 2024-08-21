package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.RestBean;
import org.cubewhy.chat.entity.Role;
import org.cubewhy.chat.entity.dto.RegisterDTO;
import org.cubewhy.chat.entity.vo.AccountVO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.RoleService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Value("${spring.application.register.state}")
    boolean allowRegister;

    @Resource
    AccountService accountService;

    @Resource
    PasswordEncoder passwordEncoder;

    @Resource
    RoleService roleService;

    @PostMapping("register")
    public ResponseEntity<RestBean<AccountVO>> register(@RequestBody RegisterDTO dto) {
        if (accountService.existByUsername(dto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(RestBean.failure(409, "Conflict"));
        }
        Set<Role> roles;
        Set<Role> roles1 = accountService.useInviteCode(dto.getInviteCode());
        if (!allowRegister) {
            if (dto.getInviteCode() == null) {
                return ResponseEntity.status(400).body(RestBean.badRequest());
            } else {
                if (roles1 == null || roles1.isEmpty()) {
                    return ResponseEntity.status(400).body(RestBean.badRequest("Incorrect invite code"));
                }
                roles = roles1;
            }
        } else {
            if (roles1 != null) {
                roles = roles1;
            } else {
                Role role = roleService.createRole("MEMBER", "Member", Permission.JOIN_CHANNEL, Permission.UPLOAD_FILES, Permission.CREATE_CHANNEL, Permission.SEND_MESSAGE);
                roles = new HashSet<>();
                roles.add(role);
            }
        }

        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setPassword(passwordEncoder.encode(dto.getPassword()));
        account.setEmail(dto.getEmail());
        account.setNickname(dto.getNickname());
        account.setBio(dto.getBio());
        account.setRoles(roles);
        Account accountResult = accountService.createAccount(account);
        if (accountResult == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(RestBean.failure(409, "Name was taken"));
        }
        return ResponseEntity.ok(RestBean.success(accountResult.asViewObject(AccountVO.class, (vo) -> {
            vo.setRoles(roles.stream().map(Role::getName).toList());
        })));
    }
}
