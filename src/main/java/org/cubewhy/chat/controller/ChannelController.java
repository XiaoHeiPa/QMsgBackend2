package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.dto.ChannelDTO;
import org.cubewhy.chat.entity.dto.GenerateChannelInviteCodeDTO;
import org.cubewhy.chat.entity.vo.ChannelInviteCodeVO;
import org.cubewhy.chat.entity.vo.ChannelJoinRequestVO;
import org.cubewhy.chat.entity.vo.ChannelVO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.util.RedisConstants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/channel")
public class ChannelController {
    @Resource
    ChannelService channelService;

    @Resource
    AccountService accountService;

    @Resource
    RedisTemplate<String, ChannelInviteCodeVO> channelInviteCodeRedisTemplate;

    @PostMapping("create")
    public ResponseEntity<RestBean<ChannelVO>> createChannel(HttpServletRequest request, @RequestBody ChannelDTO createChannelDTO) {
        Channel channel = channelService.createChannel(createChannelDTO);
        Account account = (Account) request.getUserPrincipal();
        // 将所有权限给创建者
        // 这不会导致滥用,服务器权限会自动忽略
        channelService.addUserToChannel(channel, account, Permission.values());
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

    @GetMapping("request/list")
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

    @GetMapping("list")
    public ResponseEntity<RestBean<List<ChannelVO>>> listChannels(HttpServletRequest request) {
        Account account = (Account) request.getUserPrincipal();
        List<ChannelVO> list = accountService.findJoinedChannels(account).stream().map(channel -> channel.asViewObject(ChannelVO.class)).toList();
        return ResponseEntity.ok(RestBean.success(list));
    }

    @PostMapping("invite/generate")
    public ResponseEntity<RestBean<ChannelInviteCodeVO>> generateChannelInviteCode(HttpServletRequest request, @RequestBody GenerateChannelInviteCodeDTO dto) {
        long timeout = dto.getExpireAt() - System.currentTimeMillis();
        if (timeout <= 0) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.badRequest("Past time"));
        if (channelInviteCodeRedisTemplate.opsForValue().get(RedisConstants.CHANNEL_INVITATION + dto.getCode()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(RestBean.failure(409, "Conflict"));
        }
        Account account = (Account) request.getUserPrincipal();
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
    public ResponseEntity<RestBean<ChannelInviteCodeVO>> queryChannelInviteCode(HttpServletRequest request, @PathVariable String code) {
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

        Account account = (Account) request.getUserPrincipal();
        channelService.addUserToChannel(vo.getChannelId(), account.getId());
        return ResponseEntity.ok(RestBean.success());
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
