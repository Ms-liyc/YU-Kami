package com.yuchen.kami;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yuchen.kami.mapper")
public class YuKamiApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuKamiApplication.class, args);
    }
}
