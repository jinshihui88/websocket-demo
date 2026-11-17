package com.example.websocket.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 聊天消息模型
 *
 * 设计要点：
 * 1. 支持多种消息类型
 * 2. 包含时间戳信息
 * 3. 支持自动回复标识
 * 4. JSON 序列化友好
 */
public class ChatMessage {

    private String content;             // 消息内容
    private String sender;              // 发送者
    private MessageType type;           // 消息类型
    private boolean isAutoReply = false; // 是否自动回复

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;    // 时间戳

    /**
     * 消息类型枚举
     *
     * 不同类型用于：
     * - 前端显示差异化样式
     * - 后端业务逻辑判断
     * - 消息过滤和路由
     */
    public enum MessageType {
        CHAT,     // 普通聊天消息
        JOIN,     // 用户加入提醒
        LEAVE,    // 用户离开提醒
        BOT,      // 机器人自动回复
        ADMIN     // 系统管理员消息
    }

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String content, String sender, MessageType type) {
        this.content = content;
        this.sender = sender;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isAutoReply() { return isAutoReply; }
    public void setAutoReply(boolean autoReply) { this.isAutoReply = autoReply; }
}
