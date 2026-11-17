# websocket-demo

Spring Boot 3.x 整合 WebSocket（STOMP）实战教程项目：一个功能完整的聊天室，包含群聊广播、点对点私聊、机器人智能回复、连接状态监听与定时系统消息。

## 项目简介

- 基于 STOMP over WebSocket 实现前后端实时通信
- SockJS 提供降级方案（浏览器不支持 WebSocket 时自动切换 HTTP 长轮询）
- 演示广播（`/topic`）、点对点（`/user/queue`）、消息路由（`/app`）三组核心前缀的用法
- 配套教程文档：`docs/SpringBoot3整合WebSocket实战教程.md`

## 技术栈及版本

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | Spring Boot 3.x 最低要求 |
| Spring Boot | 3.5.16 | BOM 统一管理依赖版本 |
| spring-boot-starter-websocket | 3.5.16 | WebSocket + STOMP 支持 |
| spring-boot-starter-thymeleaf | 3.5.16 | 聊天页面渲染 |
| spring-boot-starter-actuator | 3.5.16 | 健康检查与监控 |
| SockJS Client（前端） | 1.6.1 | CDN 引入 |
| STOMP.js（前端） | 2.3.3 | CDN 引入 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+

### 启动

```bash
mvn spring-boot:run
```

应用启动在 `http://localhost:8080`。若该端口被占用，改 `server.port` 后同步修改前端连接地址。

### 验证

1. 浏览器访问 `http://localhost:8080/`，输入用户名进入聊天室
2. 多开几个标签页用不同用户名加入，互发消息即可看到广播效果
3. 发送"你好"或"时间"触发机器人回复
4. SockJS 端点检查：`curl http://localhost:8080/ws/info`，返回 `websocket: true` 即正常
5. 健康检查：`http://localhost:8080/actuator/health`

## 工程结构

```text
websocket-demo
├── pom.xml
├── docs/SpringBoot3整合WebSocket实战教程.md
└── src/main
    ├── java/com/example/websocket
    │   ├── WebsocketDemoApplication.java    # 启动类（@EnableScheduling）
    │   ├── config/WebSocketConfig.java      # STOMP 端点与消息代理配置
    │   ├── controller/
    │   │   ├── ChatController.java          # 群聊（@MessageMapping + @SendTo）
    │   │   ├── PrivateChatController.java   # 私聊（convertAndSendToUser）
    │   │   └── PageController.java          # 页面路由与手动推送接口
    │   ├── listener/WebSocketEventListener.java  # 连接/断开事件监听
    │   ├── model/ChatMessage.java           # 消息实体 + MessageType 枚举
    │   └── service/
    │       ├── BotReplyService.java         # 关键词匹配智能回复
    │       └── ScheduledMessageService.java # 定时系统消息
    └── resources/
        ├── application.yml
        ├── static/js/chat-client.js         # 前端 STOMP 客户端
        └── templates/chat.html              # 聊天页面
```

## 关键配置说明

- **消息路由前缀**：`/topic` 广播、`/queue` 点对点队列、`/app` 客户端发送前缀、`/user` 用户专属前缀（见 `WebSocketConfig`）
- **STOMP 端点**：`/ws`，已启用 SockJS 降级与跨域放行（生产环境务必改为具体域名）
- **端口**：`server.port=8080`
- **JSON 时间格式**：全局 `yyyy-MM-dd HH:mm:ss`，`ChatMessage.timestamp` 另有 `@JsonFormat` 兜底

## 接口 / 功能清单

| 类型 | 路径/目的地 | 说明 |
|---|---|---|
| 页面 | `GET /` | 聊天室页面 |
| STOMP 端点 | `/ws` | WebSocket 连接入口（SockJS） |
| 消息 | `/app/chat.sendMessage` | 发送群聊消息，广播到 `/topic/public` |
| 消息 | `/app/chat.addUser` | 用户加入，保存会话用户名并广播 JOIN |
| 消息 | `/app/private/{userId}` | 发送私聊消息到指定用户队列 |
| 订阅 | `/topic/public` | 公共频道广播 |
| 订阅 | `/user/queue/private` | 私聊队列 |
| HTTP | `GET /api/system/message?content=xxx` | 手动推送系统消息（测试用） |
| HTTP | `GET /actuator/health` | 健康检查 |
| 定时 | 每 30 分钟 / 每小时整点 | 系统提示与报时消息 |

## 踩坑备忘

1. **机器人死循环**：`shouldReply` 必须过滤 BOT/ADMIN 及 `isAutoReply=true` 的消息，否则机器人回复自己导致消息雪崩
2. **`@EnableScheduling` 不能漏**：只写 `@Scheduled` 不加开关，定时任务静默失效
3. **`fixedRate` 启动即执行**：已加 `initialDelay=60s`，避免应用刚启动就推送消息
4. **会话属性**：断开事件只能从 `SessionDisconnectEvent` 的会话属性里拿用户名，`addUser` 中的 `put("username", ...)` 不可省略
5. **XSS 防护**：前端渲染用户输入前必须 HTML 转义（`escapeHtml`）
6. **SimpleBroker 单机限制**：集群部署需换 `enableStompBrokerRelay` 对接 RabbitMQ/ActiveMQ

## 生产化建议

- 跨域从 `setAllowedOriginPatterns("*")` 收紧为具体域名列表
- 用户身份接入 Spring Security（Principal 绑定），替换前端自填用户名
- 敏感词过滤与消息频控，防止刷屏
- 集群场景使用外部消息代理（RabbitMQ STOMP 插件）
