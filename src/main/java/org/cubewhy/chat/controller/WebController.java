package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import org.cubewhy.chat.entity.Channel;
import org.cubewhy.chat.entity.vo.ChannelVO;
import org.cubewhy.chat.service.ChannelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {
    @Resource
    ChannelService channelService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/c/{name}")
    public String joinChannel(@PathVariable String name, Model model) {
        Channel channel = channelService.findChannelByName(name);
        if (channel == null || !channel.isPublicChannel()) {
            return "channel-not-found";
        }
        model.addAttribute("channel", channel.asViewObject(ChannelVO.class));
        return "join-channel";
    }
}
