package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.ChatMessage;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.dto.ChatMessageDTO;
import org.cubewhy.chat.entity.vo.ChatMessageVO;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.ChatMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.ZoneOffset;
import java.util.Objects;

@Log4j2
@RestController
@RequestMapping("/chat")
public class ChatController {
    @Resource
    ChatMessageService chatMessageService;
    @Resource
    ChannelService channelService;

    @MessageMapping("/send/{channel}")
    @SendTo("/topic/channel/{channel}")
    public ChatMessageVO broadcastChannelMessage(@Payload ChatMessageDTO message, @DestinationVariable int channel, StompHeaderAccessor headerAccessor) {
        Account account = (Account) Objects.requireNonNull(headerAccessor.getUser());
        if (!account.getPermissions().contains(Permission.SEND_MESSAGE)) return null;
        ChatMessage chatMessage = chatMessageService.saveMessage(message, channel, account);
        return chatMessage.asViewObject(ChatMessageVO.class);
    }


    @GetMapping("/channel/messages")
    public Flux<ChatMessageVO> getChannelMessages(HttpServletRequest request, @RequestParam int channel, @RequestParam int page, @RequestParam int size) {
        Account account = (Account) request.getUserPrincipal();
        if (!channelService.hasViewPermission(account, channel)) return null;
        return Flux.fromIterable(chatMessageService.getMessagesByChannel(channel, page, size)
                .map(chatMessage -> chatMessage.asViewObject(ChatMessageVO.class, (vo) -> {
                    vo.setTimestamp(chatMessage.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli());
                })));
    }
}
