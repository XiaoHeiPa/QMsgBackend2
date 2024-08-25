package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.dto.*;
import org.cubewhy.chat.entity.vo.*;
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

    @PostMapping("join/{name}")
    public ResponseEntity<RestBean<ChannelVO>> joinChannel(HttpServletRequest request, @PathVariable String name) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        Channel channel = channelService.findChannelByName(name);
        if (!channel.isPublicChannel() || accountService.checkPermission(account, Permission.JOIN_CHANNEL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
        }
        channelService.addUserToChannel(channel, account, Permission.SEND_MESSAGE, Permission.VIEW_CHANNEL, Permission.UPLOAD_FILES, Permission.DOWNLOAD_FILES);
        return ResponseEntity.ok(RestBean.success(channel.asViewObject(ChannelVO.class)));
    }

    @GetMapping("messages")
    public ResponseEntity<RestBean<List<ChatMessageVO>>> getChannelMessages(HttpServletRequest request, @RequestParam int channel, @RequestParam int page, @RequestParam int size) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        if (!channelService.hasViewPermission(account, channel)) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
        return ResponseEntity.ok(RestBean.success(chatMessageService.getMessagesByChannel(channel, page, size)
                .map(chatMessage -> chatMessage.asViewObject(ChatMessageVO.class, (vo) -> {
                    vo.setChannel(channelService.findChannelById(chatMessage.getChannel()).asViewObject(ChannelVO.class));
                    vo.setSender(getSender(accountService.findAccountByIdNoExtra(chatMessage.getSender()), channel));
                })).toList()));
    }

    @PostMapping("{id}/nickname")
    public ResponseEntity<RestBean<UpdateChannelNicknameVO>> setNewNickname(HttpServletRequest request, @RequestBody UpdateChannelNicknameDTO dto, @PathVariable long id) {
        Account account = accountService.findAccountByRequest(request);
        ChannelUser channelUser = channelService.findChannelUser(id, account);
        channelUser.setChannelNickname(dto.getNickname());
        ChannelUser newCu = channelService.updateChannelUser(channelUser);
        return ResponseEntity.ok(RestBean.success(new UpdateChannelNicknameVO(newCu.getChannelNickname())));
    }

    @PostMapping("{id}/description")
    public ResponseEntity<RestBean<UpdateChannelDescriptionVO>> updateChannelDescription(HttpServletRequest request, @RequestBody UpdateChannelDescriptionDTO dto, @PathVariable long id) {
        Account account = accountService.findAccountByRequest(request);
        Channel channel = channelService.findChannelById(id);
        if (!channelService.checkPermissions(account, channel, Permission.MANAGE_CHANNEL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
        }
        channel.setDescription(dto.getDescription());
        Channel newC = channelService.updateChannel(channel);
        return ResponseEntity.ok(RestBean.success(new UpdateChannelDescriptionVO(newC.getDescription())));
    }

    @PostMapping("{id}/visible")
    public ResponseEntity<RestBean<UpdateChannelVisibleVO>> updateChannelVisible(HttpServletRequest request, @RequestBody UpdateChannelVisibleDTO dto, @PathVariable long id) {
        Account account = accountService.findAccountByRequest(request);
        Channel channel = channelService.findChannelById(id);
        if (!channelService.checkPermissions(account, channel, Permission.MANAGE_CHANNEL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
        }
        channel.setPublicChannel(dto.isVisible());
        Channel newC = channelService.updateChannel(channel);
        return ResponseEntity.ok(RestBean.success(new UpdateChannelVisibleVO(newC.isPublicChannel())));
    }

    private SenderVO getSender(Account senderAccount, long channel) {
        ChannelUser channelUser = channelService.findChannelUser(channel, senderAccount);
        SenderVO sender = new SenderVO();
        sender.setId(senderAccount.getId());
        sender.setUsername(senderAccount.getUsername());
        sender.setNickname(channelUser.getChannelNickname());
        return sender;
    }

    @PostMapping("{id}/leave")
    @Transactional
    public ResponseEntity<RestBean<String>> leaveChannel(HttpServletRequest request, @PathVariable Long id) {
        Account account = accountService.findAccountByRequest(request);
        channelService.removeUserFromChannel(id, account.getId());
        return ResponseEntity.ok(RestBean.success("Successfully leave channel"));
    }

    @PostMapping("create")
    public ResponseEntity<RestBean<ChannelVO>> createChannel(HttpServletRequest request, @RequestBody ChannelDTO createChannelDTO) {
        if (createChannelDTO.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.forbidden("Invalid channel name"));
        }
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
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestBean.unauthorized("Unauthorized"));
        }
        List<ChannelVO> list = accountService.findJoinedChannels(account).stream().map(channel -> channel.asViewObject(ChannelVO.class, (vo) -> vo.setMemberCount(channel.getChannelUsers().size()))).toList();
        return ResponseEntity.ok(RestBean.success(list));
    }

    @GetMapping("{id}/myInfo")
    public ResponseEntity<RestBean<ChannelConfInfo>> getMyInfo(HttpServletRequest request, @PathVariable long id) {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(RestBean.unauthorized("Unauthorized"));
        }
        ChannelConfInfo info = new ChannelConfInfo();
        ChannelUser channelUser = channelService.findChannelUser(id, account);
        info.setNickname(channelUser.getChannelNickname());
        info.setPermissions(channelUser.getPermissions());
        return ResponseEntity.ok(RestBean.success(info));
    }

    @PostMapping("invite/generate")
    public ResponseEntity<RestBean<ChannelInviteCodeVO>> generateChannelInviteCode(HttpServletRequest request, @RequestBody GenerateChannelInviteCodeDTO dto) {
        long timeout = dto.getExpireAt() - System.currentTimeMillis();
        if (timeout <= 0 && dto.getExpireAt() != -1) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.badRequest("Past time"));
        if (channelInviteCodeRedisTemplate.opsForValue().get(RedisConstants.CHANNEL_INVITATION + dto.getCode()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(RestBean.failure(409, "Conflict"));
        }
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        Channel channel = channelService.findChannelById(dto.getChannelId());
        if (channel == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.badRequest("Channel not found"));
        }
        if (!channelService.checkPermissions(account, channel, Permission.SEND_CHANNEL_INVITE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
        }
        ChannelInviteCodeVO vo = new ChannelInviteCodeVO();
        vo.setCode(dto.getCode());
        vo.setCreateUser(account.getId());
        vo.setChannelId(dto.getChannelId());
        vo.setExpireAt(dto.getExpireAt());
        vo.setExpireAfterUse(dto.isExpireAfterUse());
        // cache with redis
        if (dto.getExpireAt() == -1) {
            channelInviteCodeRedisTemplate.opsForValue().set(RedisConstants.CHANNEL_INVITATION + vo.getCode(), vo);
        } else {
            channelInviteCodeRedisTemplate.opsForValue().set(RedisConstants.CHANNEL_INVITATION + vo.getCode(), vo, timeout, TimeUnit.MILLISECONDS);
        }
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
