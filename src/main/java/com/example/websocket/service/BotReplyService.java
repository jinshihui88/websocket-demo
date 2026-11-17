package com.example.websocket.service;

import com.example.websocket.model.ChatMessage;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 智能回复服务
 *
 * 功能：
 * 1. 关键词匹配回复
 * 2. 防止循环回复
 * 3. 多样化回复内容
 * 4. 系统消息生成
 */
@Service
public class BotReplyService {

    private final Map<String, String[]> replyMap = new HashMap<>();
    private final Random random = new Random();

    public BotReplyService() {
        initializeReplies();
    }

    /**
     * 初始化回复规则
     *
     * 设计思路：
     * 1. 关键词映射多个回复选项
     * 2. 随机选择回复内容
     * 3. 支持中英文关键词
     * 4. 包含实用功能（时间查询等）
     */
    private void initializeReplies() {
        // 问候语回复
        replyMap.put("你好", new String[]{
                "你好！欢迎来到聊天室！",
                "嗨！很高兴见到你！",
                "你好呀！今天过得怎么样？"
        });

        replyMap.put("hello", new String[]{
                "Hello! Welcome!",
                "Hi there!",
                "Hello! Nice to meet you!"
        });

        // 功能性回复
        replyMap.put("时间", new String[]{
                "现在是 " + java.time.LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "让我看看时间..."
        });

        replyMap.put("帮助", new String[]{
                "我可以回答一些简单的问题，试试说：你好、时间、天气等",
                "需要帮助吗？我会尽力回答你的问题！"
        });

        replyMap.put("天气", new String[]{
                "我这里没有实时天气数据，建议看看天气预报哦！",
                "天气怎么样？出门记得看天穿衣！"
        });

        // 更多回复规则...
    }

    /**
     * 生成智能回复
     *
     * @param userMessage 用户消息
     * @return 回复消息，无匹配时返回 null
     */
    public ChatMessage generateReply(ChatMessage userMessage) {
        String content = userMessage.getContent().toLowerCase().trim();

        // 遍历回复规则，查找匹配
        for (Map.Entry<String, String[]> entry : replyMap.entrySet()) {
            if (content.contains(entry.getKey())) {
                String[] replies = entry.getValue();
                String reply = replies[random.nextInt(replies.length)];

                // 创建回复消息
                ChatMessage botMessage = new ChatMessage();
                botMessage.setContent(reply);
                botMessage.setSender("智能助手");
                botMessage.setType(ChatMessage.MessageType.BOT);
                botMessage.setAutoReply(true);

                return botMessage;
            }
        }

        return null; // 无匹配回复
    }

    /**
     * 判断是否应该回复
     *
     * 防止循环回复的重要逻辑：
     * 1. 不回复机器人自己的消息
     * 2. 不回复管理员消息
     * 3. 不回复自动回复消息
     * 4. 只回复普通聊天消息
     */
    public boolean shouldReply(ChatMessage message) {
        // 避免循环回复
        if (message.getType() == ChatMessage.MessageType.BOT ||
            message.getType() == ChatMessage.MessageType.ADMIN ||
            message.isAutoReply()) {
            return false;
        }

        // 只回复聊天消息
        return message.getType() == ChatMessage.MessageType.CHAT;
    }

    /**
     * 创建系统广播消息
     */
    public ChatMessage createSystemBroadcast(String content) {
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setContent(content);
        systemMessage.setSender("系统");
        systemMessage.setType(ChatMessage.MessageType.ADMIN);
        systemMessage.setAutoReply(true);

        return systemMessage;
    }
}
