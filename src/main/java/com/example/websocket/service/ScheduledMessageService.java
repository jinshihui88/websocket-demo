package com.example.websocket.service;

import com.example.websocket.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 定时消息服务
 *
 * 功能：
 * 1. 定时发送系统提醒
 * 2. 时间报时功能
 * 3. 手动消息推送
 */
@Service
public class ScheduledMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledMessageService.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final BotReplyService botReplyService;

    private final String[] scheduledMessages = {
            "💡 小贴士：输入\"帮助\"获取更多聊天技巧",
            "🎉 欢迎大家来到聊天室畅所欲言！",
            "⏰ 定时提醒：记得适当休息，保护眼睛哦",
            "🤖 我是智能助手，有什么问题可以问我",
            "⭐ 今天也要保持好心情呢！",
            "📡 聊天室运行正常，感谢大家的使用"
    };

    private final Random random = new Random();

    public ScheduledMessageService(SimpMessageSendingOperations messagingTemplate,
                                   BotReplyService botReplyService) {
        this.messagingTemplate = messagingTemplate;
        this.botReplyService = botReplyService;
    }

    /**
     * 定时发送系统消息
     *
     * @Scheduled: Spring 定时任务注解
     * fixedRate: 固定间隔执行（毫秒）
     */
    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay = 60 * 1000) // 每30分钟，启动1分钟后首次执行
    public void sendScheduledMessage() {
        try {
            String content = scheduledMessages[random.nextInt(scheduledMessages.length)];
            ChatMessage systemMessage = botReplyService.createSystemBroadcast(content);

            messagingTemplate.convertAndSend("/topic/public", systemMessage);
            logger.info("发送定时消息: {}", content);
        } catch (Exception e) {
            logger.error("发送定时消息失败", e);
        }
    }

    /**
     * 每小时时间提醒
     *
     * cron: 使用 cron 表达式定时
     * "0 0 * * * ?": 每小时整点执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void sendHourlyTimeReminder() {
        try {
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String content = String.format("🕐 当前时间: %s, 大家聊天愉快！", currentTime);

            ChatMessage timeMessage = botReplyService.createSystemBroadcast(content);
            messagingTemplate.convertAndSend("/topic/public", timeMessage);
            logger.info("发送时间提醒: {}", content);
        } catch (Exception e) {
            logger.error("发送时间提醒失败", e);
        }
    }

    /**
     * 手动发送系统消息
     *
     * 供管理接口调用
     */
    public void sendSystemMessage(String content) {
        try {
            ChatMessage systemMessage = botReplyService.createSystemBroadcast(content);
            messagingTemplate.convertAndSend("/topic/public", systemMessage);
            logger.info("发送系统消息: {}", content);
        } catch (Exception e) {
            logger.error("发送系统消息失败", e);
        }
    }
}
