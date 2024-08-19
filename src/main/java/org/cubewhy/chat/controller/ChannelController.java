package org.cubewhy.chat.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.RestBean;
import org.cubewhy.chat.entity.dto.ChannelDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/channel")
public class ChannelController {
    @PostMapping("create")
    public ResponseEntity<RestBean<ChannelVO>> createChannel(HttpServletRequest request, @RequestBody ChannelDTO createChannelDTO) {
        Account account = (Account) request.getUserPrincipal();

    }
}
