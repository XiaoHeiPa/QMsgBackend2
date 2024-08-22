package org.cubewhy.chat.controller;

import org.cubewhy.chat.entity.RestBean;
import org.cubewhy.chat.entity.vo.StatusVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/status")
public class StatusController {
    @GetMapping("check")
    public RestBean<StatusVO> check() {
        StatusVO status = new StatusVO();
        status.setTimestamp(System.currentTimeMillis());
        return RestBean.success(status);
    }
}
