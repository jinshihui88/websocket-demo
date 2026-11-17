package com.example.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 主应用类
 *
 * @EnableScheduling: 启用定时任务支持，配合 ScheduledMessageService 使用
 */
@SpringBootApplication
@EnableScheduling
public class WebsocketDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketDemoApplication.class, args);
    }
}
