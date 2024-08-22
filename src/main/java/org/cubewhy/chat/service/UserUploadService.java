package org.cubewhy.chat.service;

import org.cubewhy.chat.entity.Account;
import org.cubewhy.chat.entity.DownloadKey;
import org.cubewhy.chat.entity.UserUpload;

import java.io.IOException;

public interface UserUploadService {
    UserUpload upload(byte[] bytes, String fileName, Account uploadUser, String description) throws IOException;

    default byte[] read(UserUpload userUpload) throws IOException {
        return read(userUpload.getHash());
    }

    byte[] read(String hash) throws IOException;

    UserUpload findByHash(String hash);

    boolean isValidKey(String accessKey, String fileHash);

    DownloadKey generateKey(String hash);
}


