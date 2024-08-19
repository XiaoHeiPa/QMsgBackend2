package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.UserUpload;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserUploadService {
    UserUpload upload(MultipartFile file, Account uploadUser, String description) throws IOException;

    default UserUpload upload(MultipartFile file, Account uploadUser) throws IOException {
        return upload(file, uploadUser, null);
    }

    default byte[] read(UserUpload userUpload) throws IOException {
        return read(userUpload.getHash());
    }

    byte[] read(String hash) throws IOException;

    UserUpload findByHash(String hash);
}
