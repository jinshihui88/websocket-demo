package com.example.websocket.controller;

import com.example.websocket.model.ChatMessage;
import com.example.websocket.service.BotReplyService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 聊天控制器
 *
 * 功能：
 * 1. 处理客户端消息
 * 2. 管理用户会话
 * 3. 触发智能回复
 * 4. 消息广播转发
 */
@Controller
public class ChatController {

    private final BotReplyService botReplyService;              // 智能回复服务
    private final SimpMessageSendingOperations messagingTemplate; // 消息发送模板

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public ChatController(BotReplyService botReplyService,
                          SimpMessageSendingOperations messagingTemplate) {
        this.botReplyService = botReplyService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 处理聊天消息
     *
     * @MessageMapping: 映射客户端发送的消息路径
     * @SendTo: 指定消息广播的目标路径
     * @Payload: 自动反序列化请求体为 ChatMessage 对象
     */
    @MessageMapping("/chat.sendMessage")     // 客户端发送到 /app/chat.sendMessage
    @SendTo("/topic/public")                 // 广播到 /topic/public
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        // 异步处理智能回复，避免阻塞消息转发
        handleBotReply(chatMessage);

        // 返回消息会自动广播到 /topic/public
        return chatMessage;
    }

    /**
     * 处理用户加入
     *
     * 功能：
     * 1. 保存用户会话信息
     * 2. 发送欢迎消息
     * 3. 广播用户加入提醒
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // 在 WebSocket 会话中保存用户名
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        // 异步发送个性化欢迎消息
        sendWelcomeMessage(chatMessage.getSender());

        return chatMessage;
    }

    /**
     * 处理智能回复逻辑
     *
     * 特点：
     * 1. 异步处理，不阻塞主消息流
     * 2. 模拟延迟，模拟真实对话
     * 3. 智能过滤，避免循环回复
     */
    private void handleBotReply(ChatMessage userMessage) {
        if (botReplyService.shouldReply(userMessage)) {
            // 1-3 秒随机延迟，模拟思考时间
            long delay = 1 + (long) (Math.random() * 3);

            scheduler.schedule(() -> {
                ChatMessage reply = botReplyService.generateReply(userMessage);
                if (reply != null) {
                    // 发送机器人回复到公共频道
                    messagingTemplate.convertAndSend("/topic/public", reply);
                }
            }, delay, TimeUnit.SECONDS);
        }
    }

    /**
     * 发送个性化欢迎消息
     */
    private void sendWelcomeMessage(String username) {
        scheduler.schedule(() -> {
            ChatMessage welcomeMessage = new ChatMessage();
            welcomeMessage.setContent(String.format("欢迎 %s 加入聊天室！👋 输入\"帮助\"查看可用命令", username));
            welcomeMessage.setSender("智能助手");
            welcomeMessage.setType(ChatMessage.MessageType.BOT);
            welcomeMessage.setAutoReply(true);

            messagingTemplate.convertAndSend("/topic/public", welcomeMessage);
        }, 2, TimeUnit.SECONDS);
    }
}
