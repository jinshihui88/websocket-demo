package com.example.websocket.listener;

import com.example.websocket.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket 事件监听器
 *
 * 功能：
 * 1. 监听用户连接/断开事件
 * 2. 广播用户状态变化
 * 3. 清理会话资源
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 处理用户连接事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("新用户连接: sessionId={}",
                event.getMessage().getHeaders().get("simpSessionId"));
    }

    /**
     * 处理用户断开连接事件
     *
     * 重要功能：
     * 1. 获取断开用户信息
     * 2. 广播用户离开消息
     * 3. 清理会话资源
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            logger.info("用户断开连接: {}", username);

            // 创建用户离开消息
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);

            // 广播用户离开消息
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
