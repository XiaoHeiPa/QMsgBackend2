package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Channel;
import org.cubewhy.chat.entity.ChatMessage;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.dto.RecallMessageDTO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.ChatMessageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Resource
    ChatMessageService chatMessageService;

    @Resource
    ChannelService channelService;

    @Resource
    AccountService accountService;

    @DeleteMapping("delete")
    public void deleteMessages(HttpServletRequest request, @RequestBody RecallMessageDTO dto) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        dto.getMessageId().stream().map(id -> chatMessageService.findMessageById(id)).forEach(message -> {
            Channel channel = channelService.findChannelById(message.getChannel());
            if (message.getSender() == account.getId() || channelService.checkPermissions(account, channel, Permission.MANAGE_CHANNEL)) {
                chatMessageService.deleteMessage(message);
            }
        });
    }
}
