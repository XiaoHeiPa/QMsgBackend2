package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.cubewhy.chat.entity.*;
import org.cubewhy.chat.entity.dto.CheckFileDTO;
import org.cubewhy.chat.entity.vo.DownloadKeyVO;
import org.cubewhy.chat.entity.vo.UserUploadVO;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.UserUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @Resource
    UserUploadService userUploadService;

    @Resource
    AccountService accountService;

    @PostMapping("upload")
    public ResponseEntity<RestBean<UserUploadVO>> upload(HttpServletRequest request, MultipartFile file) throws Exception {
        Account account = accountService.findAccountById((int) request.getAttribute("id"));
        UserUpload upload = userUploadService.upload(file, account);
        UserUploadVO uu = upload.asViewObject(UserUploadVO.class, (vo) -> {
            vo.setUploadUser(account.getId());
        });
        return ResponseEntity.ok(RestBean.success(uu));
    }

    @GetMapping("check")
    public ResponseEntity<RestBean<UserUploadVO>> check(HttpServletRequest request, CheckFileDTO checkFileDTO) throws Exception {
        UserUpload userUpload = userUploadService.findByHash(checkFileDTO.getHash());
        if (userUpload == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Not Found"));
        return ResponseEntity.ok(RestBean.success(userUpload.asViewObject(UserUploadVO.class, (vo) -> {
            vo.setUploadUser(userUpload.getUploadUser().getId());
        })));
    }

    @GetMapping("download/{hash}")
    public void download(HttpServletResponse response, @PathVariable String hash, @RequestParam(name = "key") String accessKey) throws Exception {
        if (!userUploadService.isValidKey(accessKey, hash)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(RestBean.failure(403, "Forbidden").toJson());
        }
        UserUpload userUpload = userUploadService.findByHash(hash);
        byte[] bytes = userUploadService.read(userUpload);
        StreamUtils.copy(bytes, response.getOutputStream());
    }

    @GetMapping("download/{hash}/key")
    public ResponseEntity<RestBean<DownloadKeyVO>> generateDownloadKey(@PathVariable String hash) {
        DownloadKey key = userUploadService.generateKey(hash);
        DownloadKeyVO keyVO = key.asViewObject(DownloadKeyVO.class);
        return ResponseEntity.ok(RestBean.success(keyVO));
    }
}
