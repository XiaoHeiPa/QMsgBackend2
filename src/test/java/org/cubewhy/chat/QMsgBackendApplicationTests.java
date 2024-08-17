package org.cubewhy.chat;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Channel;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.Role;
import org.cubewhy.chat.entity.dto.ChannelDTO;
import org.cubewhy.chat.entity.dto.ChatMessageDTO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.ChatMessageService;
import org.cubewhy.chat.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class QMsgBackendApplicationTests {
    @Resource
    BCryptPasswordEncoder passwordEncoder;
    @Resource
    AccountService accountService;
    @Resource
    RoleService roleService;
    @Resource
    ChannelService channelService;
    @Resource
    ChatMessageService chatMessageService;

    @Test
    void contextLoads() {
        Role role = roleService.createRole("USER1", "Default", Permission.CREATE_CHANNEL, Permission.SEND_MESSAGE, Permission.JOIN_CHANNEL);
        Account account = accountService.createAccount("test1", "test", role);
        Channel channel = channelService.createChannel(ChannelDTO.builder()
                .title("Test")
                .name("test")
                .description("test")
                .build());
        System.out.println(channel.getName() + " "+ channel.getId());
        channelService.addUserToChannel(channel.getId(), account.getId(), Permission.CREATE_CHANNEL);
        ChatMessageDTO message = new ChatMessageDTO();
        message.setContentType("hello-world");
        message.setContent(JSON.parseObject("{\"hello\":\"world\"}"));
        chatMessageService.saveMessage(message, channel.getId(), account);
    }

    @Test
    void testEncodePassword() {
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println(encodedPassword);
        assert passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
