# Миссия #9 — Spring WebSocket: real-time чат

## Что строим

Групповой чат в реальном времени. Сообщение отправлено — мгновенно появляется у всех подключённых пользователей. История хранится в PostgreSQL.

**Что ты скажешь клиенту:** "Реализовал real-time коммуникацию через WebSocket + STOMP. Применимо для чатов, live-уведомлений, дашбордов с обновлением в реальном времени."

## Как работает WebSocket vs HTTP

```
HTTP:       Клиент → запрос → Сервер → ответ → соединение закрыто
WebSocket:  Клиент ←→ Сервер (постоянное двустороннее соединение)
```

STOMP (Simple Text Oriented Message Protocol) — протокол поверх WebSocket для маршрутизации сообщений.

## Архитектура

```
Browser 1 ──────────┐
Browser 2 ──────────┤──── [WebSocket сервер] ──── [Message Broker] ──── все подписчики
Browser 3 ──────────┘         Spring Boot           (встроенный)
```

## Конфигурация WebSocket

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");    // сюда приходят сообщения подписчикам
        registry.setApplicationDestinationPrefixes("/app"); // сюда клиент отправляет
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")              // URL для подключения
            .withSockJS();                       // fallback для браузеров без WebSocket
    }
}
```

## Controller для WebSocket

```java
@Controller
public class ChatController {
    
    // Клиент отправляет на /app/chat.send
    @MessageMapping("/chat.send")
    @SendTo("/topic/public")    // рассылаем всем подписчикам /topic/public
    public ChatMessage sendMessage(ChatMessage message) {
        messageService.save(message);
        return message;
    }
    
    // Клиент подключается: /app/chat.join
    @MessageMapping("/chat.join")
    @SendTo("/topic/public")
    public ChatMessage joinChat(ChatMessage message) {
        message.setType(MessageType.JOIN);
        return message;
    }
}
```

## JS клиент (простой HTML файл)

```javascript
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function() {
    // Подписываемся на канал
    stompClient.subscribe('/topic/public', function(message) {
        showMessage(JSON.parse(message.body));
    });
});

// Отправить сообщение
function sendMessage(text) {
    stompClient.send('/app/chat.send', {}, JSON.stringify({
        sender: username,
        content: text,
        type: 'CHAT'
    }));
}
```

## Message entity

```java
@Entity
public class ChatMessage {
    @Id @GeneratedValue Long id;
    String sender;
    String content;
    @Enumerated(EnumType.STRING) MessageType type;  // CHAT, JOIN, LEAVE
    LocalDateTime timestamp;
}
```

## REST эндпоинт — история
```
GET /api/messages?limit=50  — последние 50 сообщений при загрузке страницы
```

## Что объяснять

- Разница HTTP vs WebSocket (pull vs push)
- Зачем STOMP поверх WebSocket (топики, маршрутизация)
- `@MessageMapping` vs `@GetMapping` — разные протоколы
- SockJS — fallback для браузеров/сетей где WebSocket заблокирован

## Ветка

```bash
git checkout -b project/9-websocket
```
