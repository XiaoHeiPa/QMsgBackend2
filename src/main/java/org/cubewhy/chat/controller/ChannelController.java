package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Channel;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.RestBean;
import org.cubewhy.chat.entity.dto.ChannelDTO;
import org.cubewhy.chat.entity.vo.ChannelVO;
import org.cubewhy.chat.service.ChannelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;

@RestController
@RequestMapping("/channel")
public class ChannelController {
    @Resource
    ChannelService channelService;

    @PostMapping("create")
    public ResponseEntity<RestBean<ChannelVO>> createChannel(HttpServletRequest request, @RequestBody ChannelDTO createChannelDTO) {
        Account account = (Account) request.getUserPrincipal();
        if (!account.getPermissions().contains(Permission.CREATE_CHANNEL)) return ResponseEntity.status(403).body(RestBean.forbidden("Forbidden"));
        Channel channel = channelService.createChannel(createChannelDTO);
        return ResponseEntity.ok(RestBean.success(channel.asViewObject(ChannelVO.class, (vo) -> {
            vo.setCreatedAt(channel.getCreateAt().toInstant(ZoneOffset.UTC).toEpochMilli());
        })));
    }
}
