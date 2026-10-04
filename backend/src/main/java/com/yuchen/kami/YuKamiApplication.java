package com.yuchen.kami;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.yuchen.kami.mapper")
@EnableScheduling
public class YuKamiApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuKamiApplication.class, args);
    }
}
