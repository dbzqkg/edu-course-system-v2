package com.lzh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // 开启异步
@EnableAspectJAutoProxy // 开启 AOP
public class EduServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduServerApplication.class, args);
    }

}
