package org.example.knockin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KnockInApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnockInApplication.class, args);
    }

}
