package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.dto.ChannelDTO;
import org.cubewhy.chat.entity.dto.ChannelJoinRequestDTO;
import org.cubewhy.chat.entity.vo.ChannelJoinRequestVO;
import org.cubewhy.chat.entity.vo.ChannelVO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/channel")
public class ChannelController {
    @Resource
    ChannelService channelService;
    @Resource
    AccountService accountService;

    @PostMapping("create")
    public ResponseEntity<RestBean<ChannelVO>> createChannel(HttpServletRequest request, @RequestBody ChannelDTO createChannelDTO) {
        Channel channel = channelService.createChannel(createChannelDTO);
        return ResponseEntity.ok(RestBean.success(channel.asViewObject(ChannelVO.class, (vo) -> {
            vo.setCreatedAt(channel.getCreateAt().toInstant(ZoneOffset.UTC).toEpochMilli());
        })));
    }

    @PostMapping("request/{id}/approve")
    public ResponseEntity<RestBean<String>> approveJoinRequest(HttpServletRequest request, @PathVariable long id) {
        ChannelJoinRequest joinRequest = joinRequestOrNull(request, id);
        if (joinRequest == null) {
            // no permission
            return ResponseEntity.status(403).body(RestBean.forbidden("Forbidden"));
        }
        if (channelService.approveJoinRequest(joinRequest)) {
            return ResponseEntity.ok(RestBean.success("Approved"));
        }
        return ResponseEntity.status(400).body(RestBean.badRequest("RequestId not found"));
    }

    @PostMapping("request/{id}/reject")
    public ResponseEntity<RestBean<String>> rejectJoinRequest(HttpServletRequest request, @PathVariable long id) {
        ChannelJoinRequest joinRequest = joinRequestOrNull(request, id);
        if (joinRequest == null) {
            // no permission
            return ResponseEntity.status(403).body(RestBean.forbidden("Forbidden"));
        }
        if (channelService.rejectJoinRequest(joinRequest)) {
            return ResponseEntity.ok(RestBean.success("Rejected"));
        }
        return ResponseEntity.status(400).body(RestBean.badRequest("RequestId not found"));
    }

    @PostMapping("request/list")
    public ResponseEntity<RestBean<List<ChannelJoinRequestVO>>> listPaddingJoinRequests(HttpServletRequest request) {
        Account account = (Account) request.getUserPrincipal();
        List<ChannelJoinRequestVO> requests;
        if (accountService.checkPermission(account, Permission.MANAGE_CHANNEL)) {
            // 是服务器管理员,返回全部加频道请求
            requests = channelService.findAllJoinRequests().stream().map((request1) ->
                    request1.asViewObject(ChannelJoinRequestVO.class)
            ).toList();
        } else {
            requests = channelService.findJoinRequestsByAccount(account).stream().map((request1) ->
                    request1.asViewObject(ChannelJoinRequestVO.class)
            ).toList();
        }
        return ResponseEntity.ok(RestBean.success(requests));
    }

    private ChannelJoinRequest joinRequestOrNull(HttpServletRequest request, long requestId) {
        Account account = (Account) request.getUserPrincipal();
        ChannelJoinRequest joinRequest = channelService.findJoinRequestById(requestId);
        Channel channel = channelService.findChannelById(joinRequest.getChannelId());
        if (channelService.checkPermissions(account, channel, Permission.MANAGE_CHANNEL) && accountService.checkPermission(account, Permission.MANAGE_CHANNEL)) {
            return joinRequest;
        }
        return null;
    }
}
