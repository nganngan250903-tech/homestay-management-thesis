package com.example.homestaymanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HomestayManagerApplication {

    public static void main(String[] args) {
        System.out.println("ENV HOST = " + System.getenv("DB_HOST"));
        SpringApplication.run(HomestayManagerApplication.class, args);
    }

}
