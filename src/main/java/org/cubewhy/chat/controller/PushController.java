package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.RestBean;
import org.cubewhy.chat.entity.dto.UpdateFirebaseTokenDTO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.util.RedisConstants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push")
public class PushController {
    @Resource
    RedisTemplate<String, String> redisTemplate;

    @Resource
    AccountService accountService;

    @PostMapping("fcm")
    public RestBean<String> updateFCMToken(HttpServletRequest request, @RequestBody UpdateFirebaseTokenDTO dto) {
        Account account = accountService.findAccountByRequest(request);
        redisTemplate.opsForValue().set(RedisConstants.FCM_TOKEN + account.getId(), dto.getToken());
        return RestBean.success("Success");
    }
}
