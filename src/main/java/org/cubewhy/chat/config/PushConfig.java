package org.cubewhy.chat.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

@Configuration
public class PushConfig {
    @Value("${spring.application.push.fcm.config}")
    String fcmConfigPath;

    @Bean
    FirebaseApp firebaseApp(GoogleCredentials credentials) {
        FirebaseOptions firebaseOptions = FirebaseOptions
                .builder()
                .setCredentials(credentials)
                .build();
        return FirebaseApp.initializeApp(firebaseOptions);
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp app) {
        return FirebaseMessaging.getInstance(app);
    }

    @Bean
    GoogleCredentials googleCredentials() throws IOException {
        return GoogleCredentials
                .fromStream(new FileInputStream(fcmConfigPath.replaceFirst("~", System.getProperty("user.home"))));
    }

}
