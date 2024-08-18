package org.cubewhy.chat.controller.admin;

import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.RestBean;
import org.cubewhy.chat.entity.Role;
import org.cubewhy.chat.entity.dto.AccountDTO;
import org.cubewhy.chat.entity.dto.DeleteAccountDTO;
import org.cubewhy.chat.entity.dto.UpdatePasswordDTO;
import org.cubewhy.chat.entity.dto.UpdateRoleDTO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Log4j2
@RestController
@RequestMapping("/admin/user")
public class UserAdminController {
    @Resource
    AccountService accountService;
    @Resource
    RoleService roleService;
    @Resource
    PasswordEncoder passwordEncoder;

    @GetMapping("list")
    public List<Account> list() {
        return accountService.findAllAccounts();
    }

    @PostMapping("add")
    public ResponseEntity<RestBean<String>> addAccount(@RequestBody AccountDTO accountDTO) {
        try {
            Account account = new Account();
            account.setUsername(accountDTO.getUsername());
            account.setPassword(accountDTO.getPassword());
            account.setBio(accountDTO.getBio());
            account.setEmail(accountDTO.getEmail());
            for (long role : accountDTO.getRoles()) {
                Role roleObj = roleService.findById(role);
                if (roleObj == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Role not found"));
                }
                account.getRoles().add(roleObj);
            }

            accountService.createAccount(account);

            return ResponseEntity.ok(RestBean.success("Account added successfully"));
        } catch (Exception e) {
            log.error("Error adding account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestBean.failure(500, "Error adding account"));
        }
    }

    @DeleteMapping("delete")
    public ResponseEntity<RestBean<String>> deleteAccount(@RequestBody DeleteAccountDTO accountDTO) {
        accountService.deleteAccountById(accountDTO.getId());
        return ResponseEntity.ok(RestBean.success("Account deleted successfully"));
    }

    @PostMapping("role")
    public ResponseEntity<RestBean<String>> updateRole(@RequestBody UpdateRoleDTO updateRoleDTO) {
        Account account = accountService.findAccountById(updateRoleDTO.getAccountId());
        if (account == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Account not found"));
        Set<Role> roles = new HashSet<>();
        for (long role : updateRoleDTO.getRoles()) {
            Role roleObj = roleService.findById(role);
            if (roleObj == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Role not found"));
            }
            roles.add(roleObj);
        }
        account.setRoles(roles);
        return ResponseEntity.ok(RestBean.success("Account updated successfully"));
    }

    @PostMapping("password")
    public ResponseEntity<RestBean<String>> updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
        Account account = accountService.findAccountById(updatePasswordDTO.getAccountId());
        if (account == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.failure(400, "Account not found"));
        account.setPassword(passwordEncoder.encode(updatePasswordDTO.getPassword()));
        accountService.save(account);
        return ResponseEntity.ok(RestBean.success("Account updated successfully"));
    }
}
