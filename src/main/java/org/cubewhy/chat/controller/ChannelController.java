package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.dto.ChannelDTO;
import org.cubewhy.chat.entity.dto.ChannelJoinRequestDTO;
import org.cubewhy.chat.entity.dto.GenerateChannelInviteCodeDTO;
import org.cubewhy.chat.entity.vo.ChannelInviteCodeVO;
import org.cubewhy.chat.entity.vo.ChannelJoinRequestVO;
import org.cubewhy.chat.entity.vo.ChannelVO;
import org.cubewhy.chat.entity.vo.ChatMessageVO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.ChatMessageService;
import org.cubewhy.chat.util.RedisConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/channel")
public class ChannelController {
    @Resource
    ChannelService channelService;

    @Resource
    AccountService accountService;

    @Resource
    ChatMessageService chatMessageService;

    @Resource
    RedisTemplate<String, ChannelInviteCodeVO> channelInviteCodeRedisTemplate;

    @GetMapping("messages")
    public List<ChatMessageVO> getChannelMessages(HttpServletRequest request, @RequestParam int channel, @RequestParam int page, @RequestParam int size) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        if (!channelService.hasViewPermission(account, channel)) return null;
        return chatMessageService.getMessagesByChannel(channel, page, size)
                .map(chatMessage -> chatMessage.asViewObject(ChatMessageVO.class, (vo) -> {
                    vo.setChannel(channelService.findChannelById(chatMessage.getChannel()).asViewObject(ChannelVO.class));
                })).toList();
    }

    @PostMapping("create")
    public ResponseEntity<RestBean<ChannelVO>> createChannel(HttpServletRequest request, @RequestBody ChannelDTO createChannelDTO) {
        Channel channel = channelService.createChannel(createChannelDTO);
        if (channel == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(RestBean.failure(409, "Conflict"));
        }
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        // 将所有权限给创建者
        // 这不会导致滥用,服务器权限会自动忽略
        channelService.addUserToChannel(channel, account, Permission.values());
        return ResponseEntity.ok(RestBean.success(channel.asViewObject(ChannelVO.class)));
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

    @GetMapping("request/list")
    public ResponseEntity<RestBean<List<ChannelJoinRequestVO>>> listPaddingJoinRequests(HttpServletRequest request) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
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

    @GetMapping("list")
    public ResponseEntity<RestBean<List<ChannelVO>>> listChannels(HttpServletRequest request) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        List<ChannelVO> list = accountService.findJoinedChannels(account).stream().map(channel -> channel.asViewObject(ChannelVO.class, (vo) -> vo.setMemberCount(channel.getChannelUsers().size()))).toList();
        return ResponseEntity.ok(RestBean.success(list));
    }

    @PostMapping("invite/generate")
    public ResponseEntity<RestBean<ChannelInviteCodeVO>> generateChannelInviteCode(HttpServletRequest request, @RequestBody GenerateChannelInviteCodeDTO dto) {
        long timeout = dto.getExpireAt() - System.currentTimeMillis();
        if (timeout <= 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.badRequest("Past time"));
        if (channelInviteCodeRedisTemplate.opsForValue().get(RedisConstants.CHANNEL_INVITATION + dto.getCode()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(RestBean.failure(409, "Conflict"));
        }
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        ChannelInviteCodeVO vo = new ChannelInviteCodeVO();
        vo.setCode(dto.getCode());
        vo.setCreateUser(account.getId());
        vo.setChannelId(dto.getChannelId());
        vo.setExpireAt(dto.getExpireAt());
        vo.setExpireAfterUse(dto.isExpireAfterUse());
        // cache with redis
        channelInviteCodeRedisTemplate.opsForValue().set(RedisConstants.CHANNEL_INVITATION + vo.getCode(), vo, timeout, TimeUnit.MILLISECONDS);
        return ResponseEntity.ok(RestBean.success(vo));
    }

    @GetMapping("invite/{code}/query")
    public ResponseEntity<RestBean<ChannelInviteCodeVO>> queryChannelInviteCode(@PathVariable String code) {
        ChannelInviteCodeVO vo = channelInviteCodeRedisTemplate.opsForValue().get(RedisConstants.CHANNEL_INVITATION + code);
        if (vo == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Not found"));
        return ResponseEntity.ok(RestBean.success(vo));
    }

    @PostMapping("invite/{code}/use")
    public ResponseEntity<RestBean<String>> useChannelInviteCode(HttpServletRequest request, @PathVariable String code) {
        String key = RedisConstants.CHANNEL_INVITATION + code;
        ChannelInviteCodeVO vo = channelInviteCodeRedisTemplate.opsForValue().get(key);
        if (vo == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Not found"));
        if (vo.isExpireAfterUse()) channelInviteCodeRedisTemplate.delete(key);

        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        channelService.addUserToChannel(vo.getChannelId(), account.getId());
        return ResponseEntity.ok(RestBean.success());
    }

    @PostMapping("request")
    public ResponseEntity<RestBean<ChannelJoinRequest>> requestJoin(HttpServletRequest request, @RequestBody ChannelJoinRequestDTO dto) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        return ResponseEntity.ok(RestBean.success(channelService.createJoinRequest(dto, account)));
    }

    @PostMapping("request/{requestId}/approve")
    public ResponseEntity<RestBean<String>> approveRequest(HttpServletRequest request, @PathVariable int requestId) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        ChannelJoinRequest channelJoinRequest = channelService.findJoinRequestById(requestId);
        Channel channel = channelService.findChannelById(channelJoinRequest.getChannelId());
        if (channelService.checkPermissions(account, channel, Permission.MANAGE_CHANNEL)) {
            if (channelService.approveJoinRequest(channelJoinRequest)) {
                return ResponseEntity.ok(RestBean.success("Success"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(RestBean.failure(500, "Internal server error"));
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
    }

    private @Nullable ChannelJoinRequest joinRequestOrNull(@NotNull HttpServletRequest request, long requestId) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        ChannelJoinRequest joinRequest = channelService.findJoinRequestById(requestId);
        Channel channel = channelService.findChannelById(joinRequest.getChannelId());
        if (channelService.checkPermissions(account, channel, Permission.MANAGE_CHANNEL) && accountService.checkPermission(account, Permission.MANAGE_CHANNEL)) {
            return joinRequest;
        }
        return null;
    }
}
