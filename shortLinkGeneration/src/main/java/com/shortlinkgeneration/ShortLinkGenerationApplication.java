package com.shortlinkgeneration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.shortlinkgeneration.mapper")
@EnableScheduling // 开启定时任务
@EnableAsync // 开启异步方法支持
public class ShortLinkGenerationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortLinkGenerationApplication.class, args);
    }
}
