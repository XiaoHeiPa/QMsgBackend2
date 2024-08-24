package org.cubewhy.chat.controller;

import org.cubewhy.chat.entity.RestBean;
import org.cubewhy.chat.entity.vo.MotdVO;
import org.cubewhy.chat.entity.vo.StatusVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/status")
public class StatusController {
    @Value("${spring.application.motd.title}")
    String motdTitle;

    @Value("${spring.application.motd.text}")
    String motdText;

    @Value("${spring.application.motd.state}")
    boolean motdState;

    @GetMapping("check")
    public RestBean<StatusVO> check() {
        StatusVO status = new StatusVO();
        status.setTimestamp(System.currentTimeMillis());
        if (motdState) {
            MotdVO motd = new MotdVO();
            motd.setTitle(motdTitle);
            motd.setText(motdText);

            status.setMotd(motd);
        }
        return RestBean.success(status);
    }
}
