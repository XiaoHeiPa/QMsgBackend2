package org.cubewhy.chat.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.Permission;
import org.cubewhy.chat.entity.RestBean;
import org.cubewhy.chat.entity.UserUpload;
import org.cubewhy.chat.entity.dto.CheckFileDTO;
import org.cubewhy.chat.entity.vo.UserUploadVO;
import org.cubewhy.chat.service.UserUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneOffset;

@RestController
@RequestMapping("/file")
public class FileController {
    @Resource
    UserUploadService userUploadService;

    @PostMapping("upload")
    public ResponseEntity<RestBean<UserUploadVO>> upload(HttpServletRequest request, MultipartFile file) throws Exception {
        Account account = (Account) request.getUserPrincipal();
        if (!account.getPermissions().contains(Permission.UPLOAD_FILES)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(RestBean.failure(403, "Forbidden"));
        }
        UserUpload upload = userUploadService.upload(file, account);
        UserUploadVO uu = upload.asViewObject(UserUploadVO.class, (vo) -> {
            vo.setUploadUser(account.getId());
            vo.setTimestamp(upload.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli());
        });
        return ResponseEntity.ok(RestBean.success(uu));
    }

    @GetMapping("check")
    public ResponseEntity<RestBean<UserUploadVO>> check(HttpServletRequest request, CheckFileDTO checkFileDTO) throws Exception {
        UserUpload userUpload = userUploadService.findByHash(checkFileDTO.getHash());
        if (userUpload == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RestBean.failure(404, "Not Found"));
        return ResponseEntity.ok(RestBean.success(userUpload.asViewObject(UserUploadVO.class, (vo) -> {
            vo.setTimestamp(userUpload.getTimestamp().toInstant(ZoneOffset.UTC).toEpochMilli());
            vo.setUploadUser(userUpload.getUploadUser().getId());
        })));
    }
}
