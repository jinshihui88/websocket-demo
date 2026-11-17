/**
 * WebSocket 客户端实现
 *
 * 功能：
 * 1. 建立 WebSocket 连接
 * 2. 订阅消息频道
 * 3. 发送和接收消息
 * 4. 处理连接状态
 */

let stompClient = null;
let username = null;

/**
 * 建立 WebSocket 连接
 */
function connect() {
    username = document.getElementById('username').value.trim();

    if (!username) {
        alert('请输入用户名！');
        return;
    }

    // 创建 SockJS 连接
    const socket = new SockJS('/ws');

    // 创建 STOMP 客户端
    stompClient = Stomp.over(socket);

    // 建立连接
    stompClient.connect({}, onConnected, onError);
}

/**
 * 连接成功回调
 */
function onConnected() {
    console.log('WebSocket连接成功');

    // 切换界面
    document.getElementById('loginPanel').style.display = 'none';
    document.getElementById('chatPanel').style.display = 'flex';
    const status = document.getElementById('status');
    status.textContent = '已连接';
    status.classList.add('connected');
    document.getElementById('messageInput').disabled = false;

    // 订阅公共聊天频道
    stompClient.subscribe('/topic/public', onMessageReceived);

    // 订阅私聊频道
    stompClient.subscribe('/user/queue/private', onMessageReceived);

    // 通知服务器用户加入
    stompClient.send("/app/chat.addUser", {}, JSON.stringify({
        sender: username,
        type: 'JOIN'
    }));
}

/**
 * 连接失败回调
 */
function onError(error) {
    console.error('WebSocket连接错误:', error);
    alert('连接失败，请确认服务已启动！');
}

/**
 * 接收消息
 */
function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);
    showMessage(message);
}

/**
 * 发送聊天消息
 */
function sendMessage(event) {
    if (event) event.preventDefault();

    const messageInput = document.getElementById('messageInput');
    const messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        const chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT'
        };

        // 发送消息到服务器
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}

/**
 * 断开连接
 */
function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("WebSocket连接已断开");

    // 恢复登录界面
    stompClient = null;
    document.getElementById('chatPanel').style.display = 'none';
    document.getElementById('loginPanel').style.display = 'block';
    const status = document.getElementById('status');
    status.textContent = '未连接';
    status.classList.remove('connected');
    document.getElementById('messageInput').disabled = true;
}

/**
 * 渲染消息
 */
function showMessage(message) {
    const messageArea = document.getElementById('messageArea');

    // 消息元素
    const element = document.createElement('div');
    const time = message.timestamp ? message.timestamp : new Date().toLocaleTimeString();

    switch (message.type) {
        case 'JOIN':
            element.className = 'message join';
            element.textContent = `${message.sender} 加入了聊天室`;
            break;
        case 'LEAVE':
            element.className = 'message leave';
            element.textContent = `${message.sender} 离开了聊天室`;
            break;
        case 'BOT':
        case 'ADMIN':
            element.className = 'message ' + (message.sender === username ? 'self' : 'bot');
            element.innerHTML = `<div class="meta">${escapeHtml(message.sender)} · ${time}</div>${escapeHtml(message.content)}`;
            break;
        default: // CHAT
            element.className = 'message ' + (message.sender === username ? 'self' : '');
            element.innerHTML = `<div class="meta">${escapeHtml(message.sender)} · ${time}</div>${escapeHtml(message.content)}`;
    }

    messageArea.appendChild(element);
    messageArea.scrollTop = messageArea.scrollHeight;
}

/**
 * HTML 转义，防 XSS
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text || ''));
    return div.innerHTML;
}
