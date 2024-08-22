package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.vo.UserUploadVO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.ChannelService;
import org.cubewhy.chat.service.UserUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/avatar")
public class AvatarController {
    @Resource
    UserUploadService userUploadService;

    @Resource
    AccountService accountService;

    @Resource
    ChannelService channelService;

    @PutMapping("upload/channel/{id}")
    public ResponseEntity<RestBean<UserUploadVO>> uploadForChannel(HttpServletRequest request, @RequestBody byte[] bytes, @PathVariable long id) throws Exception{
        Account account = accountService.findAccountByRequest(request);
        Channel channel = channelService.findChannelById(id);
        if (channel == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RestBean.badRequest());
        }
        if (!channelService.checkPermissions(account, channel, Permission.MANAGE_CHANNEL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.forbidden("Forbidden"));
        }
        UserUpload upload = userUploadService.upload(bytes, "avatar-" + System.currentTimeMillis() + ".png", account, "Avatar of channel " + channel.getId());
        channel.setIconHash(upload.getHash());
        channelService.update(channel);
        return ResponseEntity.ok(RestBean.success(upload.asViewObject(UserUploadVO.class, (vo) -> {
            vo.setUploadUser(account.getId());
        })));
    }

    @PutMapping("upload")
    public ResponseEntity<RestBean<UserUploadVO>> upload(HttpServletRequest request, @RequestBody byte[] bytes) throws Exception {
        Account account = accountService.findAccountByRequest(request);
        UserUpload upload = userUploadService.upload(bytes, "avatar-" + System.currentTimeMillis() + ".png", account, "Avatar of user " + account.getId());
        account.setAvatarHash(upload.getHash());
        accountService.update(account);
        return ResponseEntity.ok(RestBean.success(upload.asViewObject(UserUploadVO.class, (vo) -> {
            vo.setUploadUser(account.getId());
        })));
    }

    @GetMapping("avatar/{name}/name")
    public void getAvatar(HttpServletResponse response, @PathVariable String name) throws Exception {
        Account user = accountService.findAccountByName(name);
        String avatarHash;
        if (user == null) {
            Channel channel = channelService.findChannelByName(name);
            if (channel == null) {
                response.setContentType("application/json");
                response.getWriter().write(RestBean.failure(404, "Not found").toJson());
                return;
            }
            avatarHash = channel.getIconHash();
        }
        else {
            avatarHash = user.getAvatarHash();
        }
            response.setContentType("image/png");
        if (avatarHash == null || avatarHash.isEmpty()) {
            StreamUtils.copy(Objects.requireNonNull(getClass().getResourceAsStream("/default-avatar.png")), response.getOutputStream());
            return;
        }
        StreamUtils.copy(userUploadService.read(avatarHash), response.getOutputStream());
    }
}
