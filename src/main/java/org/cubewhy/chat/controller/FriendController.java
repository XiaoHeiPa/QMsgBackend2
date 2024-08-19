package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Channel;
import org.cubewhy.chat.entity.FriendRequest;
import org.cubewhy.chat.entity.RestBean;
import org.cubewhy.chat.entity.dto.FriendRequestDTO;
import org.cubewhy.chat.entity.vo.ChannelVO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friend")
public class FriendController {
    @Resource
    AccountService accountService;

    @Resource
    ChannelService channelService;

    @PostMapping("request")
    public ResponseEntity<RestBean<FriendRequest>> addFriend(HttpServletRequest request, @RequestBody FriendRequestDTO dto) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        return ResponseEntity.ok(RestBean.success(channelService.createFriendRequest(dto, account)));
    }

    @PostMapping("request/{requestId}/approve")
    public ResponseEntity<RestBean<ChannelVO>> approveFriend(HttpServletRequest request, @PathVariable long requestId) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        FriendRequest fq = channelService.findFriendRequestById(requestId);
        if (fq.getTo() == account.getId()) {
            Channel channel = channelService.approveFriendRequest(fq);
            return ResponseEntity.ok(RestBean.success(channel.asViewObject(ChannelVO.class)));
        }
        return ResponseEntity.status(403).body(RestBean.forbidden("Forbidden"));
    }
}
