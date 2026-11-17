package com.example.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类
 *
 * 核心功能：
 * 1. 启用 WebSocket 消息代理（STOMP）
 * 2. 注册 STOMP 端点 /ws
 * 3. 设置消息路由规则
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     *
     * 消息代理负责：
     * - 管理客户端订阅
     * - 消息路由和分发
     * - 维护连接状态
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，设置消息代理前缀
        // 客户端订阅 /topic、/queue 开头的目的地时，消息由代理广播
        config.enableSimpleBroker("/topic", "/queue");

        // 设置客户端向服务端发送消息的前缀
        // 客户端发送到 /app/xxx 的消息会被路由到 @MessageMapping("/xxx")
        config.setApplicationDestinationPrefixes("/app");

        // 设置点对点消息的前缀
        // 服务端 convertAndSendToUser 发送的消息路由到 /user/{userId}/queue/xxx
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 注册 STOMP 端点
     *
     * STOMP 端点是客户端连接 WebSocket 的入口
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 STOMP 端点
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")   // 允许跨域（生产环境应限制具体域名）
                .withSockJS();                   // 启用 SockJS 支持（提供 WebSocket 降级方案）
    }
}
