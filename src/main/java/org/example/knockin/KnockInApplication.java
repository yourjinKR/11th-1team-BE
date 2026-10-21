package org.example.knockin;

import org.example.knockin.global.auth.service.KakaoUnlinkClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.service.registry.ImportHttpServices;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@ImportHttpServices(group = "kakao", types = KakaoUnlinkClient.class)
public class KnockInApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnockInApplication.class, args);
    }

}
