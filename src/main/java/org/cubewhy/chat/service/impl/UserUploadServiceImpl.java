package org.cubewhy.chat.service.impl;

import cn.hutool.crypto.SecureUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.DownloadKey;
import org.cubewhy.chat.entity.UserUpload;
import org.cubewhy.chat.repository.UserUploadRepository;
import org.cubewhy.chat.service.AccountService;
import org.cubewhy.chat.service.UserUploadService;
import org.cubewhy.chat.util.RedisConstants;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class UserUploadServiceImpl implements UserUploadService {
    @Resource
    RedisTemplate<String, String> redisTemplate;
    @Resource
    UserUploadRepository userUploadRepository;
    @Resource
    AccountService accountService;
    @Value("${spring.application.upload.dir}")
    private String uploadDirString;
    @Value("${spring.application.upload.keyExpire}")
    Long fileExpireTimeout;

    private File uploadDir;

    @PostConstruct
    public void init() {
        uploadDir = new File(uploadDirString.replaceFirst("~", System.getProperty("user.home")));
        log.info("Upload directory: {}", uploadDir.getAbsolutePath());
        if (!uploadDir.exists()) {
            log.info("Upload directory not found, making a new one.");
            if (uploadDir.mkdirs()) {
                log.info("Made upload dir");
            }
        }
    }

    @Override
    public UserUpload upload(MultipartFile file, Account uploadUser, String description) throws IOException {
        // process upload
        byte[] bytes = file.getBytes();
        String hash = SecureUtil.sha256(new ByteArrayInputStream(bytes));
        Optional<UserUpload> exist = userUploadRepository.findByHash(hash);
        if (exist.isPresent()) return exist.get();

        // file not exist, do upload
        File target = new File(uploadDir, hash);
        if (!target.exists()) {
            FileUtils.writeByteArrayToFile(target, bytes);
        }
        UserUpload userUpload = new UserUpload();
        userUpload.setName(file.getOriginalFilename());
        userUpload.setUploadUser(uploadUser);
        userUpload.setDescription(description);
        return userUploadRepository.save(userUpload);
    }

    @Override
    public byte[] read(String hash) throws IOException {
        File target = new File(uploadDir, hash);
        if (!target.exists()) return null;
        return FileUtils.readFileToByteArray(target);
    }

    @Override
    public UserUpload findByHash(String hash) {
        return userUploadRepository.findByHash(hash).orElse(null);
    }

    @Override
    public boolean isValidKey(String accessKey, @NotNull String fileHash) {
        String hash = redisTemplate.opsForValue().get(accessKey);
        if (hash == null) return false;
        return hash.equals(fileHash);
    }

    @Override
    public DownloadKey generateKey(String hash) {
        String key = UUID.randomUUID().toString();
        DownloadKey downloadKey = new DownloadKey();
        downloadKey.setKey(key);
        downloadKey.setCreateAt(LocalDateTime.now());
        redisTemplate.opsForValue().set(RedisConstants.FILE_ACCESS_KEY + key, hash, fileExpireTimeout, TimeUnit.SECONDS);
        downloadKey.setExpireAt(LocalDateTime.now().plusSeconds(fileExpireTimeout));
        return downloadKey;
    }
}
