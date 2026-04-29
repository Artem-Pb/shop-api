# Миссия #7 — Микросервисы с Docker Compose

## Что строим

Два отдельных Spring Boot сервиса: `user-service` (управление пользователями) и `notification-service` (отправка уведомлений). Общение через REST. Всё поднимается через Docker Compose.

**Что ты скажешь клиенту:** "Реализовал микросервисную архитектуру. Два независимых сервиса, каждый со своей БД, общаются через REST. Docker Compose поднимает всё окружение."

## Архитектура

```
Клиент
   ↓
[Nginx] ← опционально, API Gateway
   ↓           ↓
[user-service]  [notification-service]
   ↓                    ↓
[postgres:5432]   [postgres:5433]  ← каждый сервис имеет свою БД!
```

**Ключевой принцип микросервисов:** каждый сервис — отдельный процесс, отдельная БД, независимый деплой.

## user-service (порт 8081)

Отвечает за пользователей. При регистрации нового пользователя — вызывает notification-service.

```java
@Service
public class UserService {
    
    private final WebClient notificationClient = WebClient.create("http://notification-service:8082");
    
    @Transactional
    public User registerUser(CreateUserRequest request) {
        User user = userRepository.save(new User(request));
        
        // Вызываем другой сервис
        notificationClient.post()
            .uri("/api/notifications/welcome")
            .bodyValue(new WelcomeNotificationRequest(user.getEmail(), user.getName()))
            .retrieve()
            .bodyToMono(Void.class)
            .block();  // синхронный вызов
        
        return user;
    }
}
```

## notification-service (порт 8082)

Принимает запросы на уведомления, логирует их (в MVP — просто сохраняем в свою БД).

```java
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @PostMapping("/welcome")
    public ResponseEntity<Void> sendWelcome(@RequestBody WelcomeRequest request) {
        notificationService.createNotification(
            request.getEmail(),
            "Добро пожаловать, " + request.getName() + "!",
            NotificationType.WELCOME
        );
        return ResponseEntity.ok().build();
    }
}
```

## Docker Compose — два сервиса

```yaml
services:
  user-service:
    build: ./user-service
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://user-db:5432/userdb
      NOTIFICATION_SERVICE_URL: http://notification-service:8082
    depends_on:
      - user-db
      - notification-service

  notification-service:
    build: ./notification-service
    ports:
      - "8082:8082"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://notification-db:5432/notificationdb
    depends_on:
      - notification-db

  user-db:
    image: postgres:15
    environment:
      POSTGRES_DB: userdb
      POSTGRES_USER: user
      POSTGRES_PASSWORD: userpass

  notification-db:
    image: postgres:15
    environment:
      POSTGRES_DB: notificationdb
      POSTGRES_USER: notif
      POSTGRES_PASSWORD: notifpass
    ports:
      - "5433:5432"  # разные порты чтобы не конфликтовали
```

## Структура репозитория

```
project-7-microservices/
├── user-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── notification-service/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── docker-compose.yml
└── README.md
```

## Что объяснять

- Монолит vs микросервисы: когда что выбирать
- Каждый сервис имеет свою БД — почему (независимость, разные схемы)
- Service discovery — как сервисы находят друг друга (через Docker hostname)
- Синхронный REST vs асинхронный (очередь) — когда что использовать
- `docker-compose.yml` `depends_on` — порядок запуска

## Ветка

```bash
git checkout -b project/7-microservices
```
