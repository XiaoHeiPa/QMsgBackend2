package org.cubewhy.chat;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;

@SpringBootTest
class QMsgBackendApplicationTests {
    @Resource
    BCryptPasswordEncoder passwordEncoder;
    @Resource
    AccountService accountService;
    @Resource
    RoleService roleService;

    @Test
    void contextLoads() {
        roleService.createRole("DEFAULT", "Default group", Permission.SEND_MESSAGE, Permission.JOIN_CHANNEL, Permission.CREATE_CHANNEL);
        accountService.createAccount("test1", "test1", Collections.singleton("DEFAULT"));
    }

    @Test
    void testEncodePassword() {
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println(encodedPassword);
        assert passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
