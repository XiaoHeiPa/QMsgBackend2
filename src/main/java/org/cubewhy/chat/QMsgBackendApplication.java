package org.cubewhy.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class QMsgBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(QMsgBackendApplication.class, args);
    }

}
