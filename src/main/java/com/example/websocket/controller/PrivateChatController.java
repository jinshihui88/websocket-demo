package com.example.websocket.controller;

import com.example.websocket.model.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * 私聊消息控制器
 *
 * 实现点对点通信功能
 */
@Controller
public class PrivateChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public PrivateChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 私聊消息发送
     *
     * @DestinationVariable: 捕获路径变量
     * convertAndSendToUser: 发送到特定用户
     */
    @MessageMapping("/private/{userId}")
    public void sendPrivateMessage(@DestinationVariable String userId,
                                   ChatMessage message) {
        // 发送到特定用户的私聊队列
        messagingTemplate.convertAndSendToUser(
                userId, "/queue/private", message);
    }
}
