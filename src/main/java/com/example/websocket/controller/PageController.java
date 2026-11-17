package com.example.websocket.controller;

import com.example.websocket.service.ScheduledMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * 页面与管理接口控制器
 *
 * 负责聊天页面路由与管理接口
 */
@Controller
public class PageController {

    private final ScheduledMessageService scheduledMessageService;

    public PageController(ScheduledMessageService scheduledMessageService) {
        this.scheduledMessageService = scheduledMessageService;
    }

    /**
     * 聊天室页面
     */
    @GetMapping("/")
    public String chatPage() {
        return "chat";
    }

    /**
     * 手动推送系统消息接口（测试用）
     */
    @ResponseBody
    @GetMapping("/api/system/message")
    public ResponseEntity<Map<String, String>> sendSystemMessage(String content) {
        String message = (content == null || content.isBlank())
                ? "管理员手动推送的系统消息"
                : content;
        scheduledMessageService.sendSystemMessage(message);
        return ResponseEntity.ok(Map.of("status", "sent", "content", message));
    }
}
