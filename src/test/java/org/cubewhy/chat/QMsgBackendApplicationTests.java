package org.cubewhy.chat;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.dto.ChannelDTO;
import org.cubewhy.chat.entity.dto.ChatMessageDTO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.ChatMessageService;
import org.cubewhy.chat.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
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
    }

    @Test
    void queryMessages() {
//        Role role = roleService.createRole("USER1", "Default", Permission.CREATE_CHANNEL, Permission.SEND_MESSAGE, Permission.JOIN_CHANNEL);
//        Account account = accountService.createAccount("test1", "test", role);
//        Channel channel = channelService.createChannel(ChannelDTO.builder()
//                .title("Test")
//                .name("test")
//                .description("test")
//                .build());
//        System.out.println(channel.getName() + " "+ channel.getId());
//        channelService.addUserToChannel(channel.getId(), account.getId(), Permission.CREATE_CHANNEL);
//        for (int i = 0; i < 150; i++) {
//            ChatMessageDTO message = new ChatMessageDTO();
//            message.setContentType("hello-world");
//            message.setContent(JSON.parseObject("{\"hello\":\"world " + i + "\"}"));
//            chatMessageService.saveMessage(message, channel.getId(), account);
//        }
//        Page<ChatMessage> messages = chatMessageService.getMessagesByChannel(channel.getId(), 0, 10);
//        messages.forEach(message -> System.out.println(message.getContent()));
    }

    @Test
    void testEncodePassword() {
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println(encodedPassword);
        assert passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
