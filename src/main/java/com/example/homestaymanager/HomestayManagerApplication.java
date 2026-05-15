package com.example.homestaymanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HomestayManagerApplication {

    public static void main(String[] args) {
        System.out.println("ENV HOST = " + System.getenv("DB_HOST"));
        SpringApplication.run(HomestayManagerApplication.class, args);
    }

}
