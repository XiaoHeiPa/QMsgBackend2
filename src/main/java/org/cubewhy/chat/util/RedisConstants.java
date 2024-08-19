package org.cubewhy.chat.util;

public interface RedisConstants {
    String JWT_BLACKLIST = "qbychat:jwt:bl:";
    String ACCOUNT_VERIFY = "qbychat:account:verify:";
    String INVITATION = "qbychat:account:invitation:";
    String CACHED_MESSAGE = "qbychat:cache:";
    String FCM_TOKEN = "qbychat:fcm:token:";
    String FILE_ACCESS_KEY = "qbychat:file:key:";
}
