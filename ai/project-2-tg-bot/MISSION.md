# Миссия #2 — Telegram-бот на Java (Spring Boot + FSM)

## Что ты строишь и зачем это уметь объяснить

Ты строишь **Telegram-бот для записи клиентов** — как если бы малый бизнес (барбершоп, юрист, тренер) хотел принимать заявки прямо в Telegram без сайта.

Клиент пишет боту → бот задаёт вопросы шаг за шагом → сохраняет заявку → уведомляет администратора.

Это уникально тем, что **Java-боты на бирже редкость**. Все пишут на Python. У тебя — надёжный корпоративный стек, который масштабируется.

**Что ты скажешь клиенту:** "Реализовал Telegram-бота для записи клиентов. Пошаговый диалог через FSM (конечный автомат состояний), данные сохраняются в PostgreSQL, администратор получает уведомление при каждой новой заявке."

---

## Что такое FSM — объяснение за 2 минуты

FSM (Finite State Machine, конечный автомат) — это когда каждый пользователь находится в одном из заранее определённых состояний, и его ответ переводит его в следующее состояние.

```
[IDLE] 
  ↓ /start
[WAITING_NAME]    ← "Как вас зовут?"
  ↓ "Артём"
[WAITING_PHONE]   ← "Введите ваш номер телефона"
  ↓ "+7 999 123 45 67"
[WAITING_DATE]    ← "Выберите дату"
  ↓ "Завтра в 15:00"
[CONFIRM]         ← "Подтвердите запись: Артём, +7999..., завтра в 15:00"
  ↓ "Подтвердить"
[IDLE]            ← "Заявка принята! Мы свяжемся с вами."
```

Каждое состояние — это enum значение. Текущее состояние каждого пользователя хранится в БД.

---

## Архитектура бота

```
Telegram сервер
      ↓ (бот polling или webhook)
[TelegramBot класс]    ← точка входа, наследует TelegramLongPollingBot
      ↓
[UpdateHandler]        ← разбирает входящее сообщение
      ↓
[CommandHandler]       ← /start, /help, /cancel
[MessageHandler]       ← обычные текстовые ответы
[CallbackHandler]      ← нажатия на inline кнопки
      ↓
[BotService]           ← бизнес-логика, определяет следующий шаг
      ↓
[UserStateService]     ← читает/пишет состояние пользователя в БД
[ApplicationService]   ← сохраняет заявку в БД
[NotificationService]  ← уведомляет администратора
```

---

## Структура пакетов

```
src/main/java/com/polybezev/bot/
├── BotApplication.java
│
├── bot/
│   ├── TelegramBot.java           ← главный класс бота
│   └── UpdateHandler.java         ← маршрутизация обновлений
│
├── handler/
│   ├── CommandHandler.java        ← обработка /команд
│   ├── MessageHandler.java        ← обработка текстовых сообщений
│   └── CallbackHandler.java       ← обработка нажатий кнопок
│
├── state/
│   └── BotState.java              ← enum всех состояний
│
├── entity/
│   ├── UserState.java             ← состояние конкретного пользователя в БД
│   └── Application.java          ← заявка на запись
│
├── repository/
│   ├── UserStateRepository.java
│   └── ApplicationRepository.java
│
├── service/
│   ├── BotService.java            ← логика переходов между состояниями
│   ├── UserStateService.java      ← CRUD для состояний
│   ├── ApplicationService.java    ← сохранение заявок
│   └── NotificationService.java   ← отправка уведомлений
│
└── config/
    └── BotConfig.java             ← @Value токен и username
```

---

## Детальный разбор компонентов

### BotState — enum состояний
```java
public enum BotState {
    IDLE,             // начальное состояние
    WAITING_NAME,     // ждём имя
    WAITING_PHONE,    // ждём телефон
    WAITING_DATE,     // ждём дату/время
    CONFIRM           // подтверждение
}
```

### UserState entity — состояние пользователя в БД
```java
@Entity
public class UserState {
    @Id
    Long chatId;              // ID чата = ID пользователя в Telegram
    @Enumerated(EnumType.STRING)
    BotState state;           // текущее состояние
    String tempName;          // временно храним имя пока не сохранили заявку
    String tempPhone;
    String tempDate;
}
```

**Зачем хранить в БД?** Бот может перезапуститься. В памяти (HashMap) данные потеряются. В БД — нет.

### Application entity — итоговая заявка
```java
@Entity
public class Application {
    @Id @GeneratedValue
    Long id;
    Long chatId;
    String clientName;
    String phone;
    String desiredDate;
    @Enumerated(EnumType.STRING)
    ApplicationStatus status;  // NEW, CONFIRMED, CANCELLED
    LocalDateTime createdAt;
}
```

### TelegramBot — главный класс
```java
@Component
public class TelegramBot extends TelegramLongPollingBot {
    
    @Override
    public void onUpdateReceived(Update update) {
        // Telegram присылает обновления: сообщения, нажатия кнопок, etc.
        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            if (text.startsWith("/")) {
                commandHandler.handle(update);
            } else {
                messageHandler.handle(update);
            }
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handle(update);
        }
    }
    
    // Метод для отправки сообщений
    public void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .build();
        execute(message);
    }
}
```

### BotService — логика переходов
```java
@Service
public class BotService {
    
    public void handleMessage(Long chatId, String text) {
        UserState userState = userStateService.getOrCreate(chatId);
        
        switch (userState.getState()) {
            case IDLE -> {
                // Не ожидаем ввода, игнорируем или подсказываем
                bot.sendMessage(chatId, "Напишите /start чтобы записаться");
            }
            case WAITING_NAME -> {
                // Пользователь ввёл имя
                userState.setTempName(text);
                userState.setState(BotState.WAITING_PHONE);
                userStateService.save(userState);
                bot.sendMessage(chatId, "Введите ваш номер телефона:");
            }
            case WAITING_PHONE -> {
                userState.setTempPhone(text);
                userState.setState(BotState.WAITING_DATE);
                userStateService.save(userState);
                // Отправляем inline кнопки с датами
                bot.sendMessageWithButtons(chatId, "Выберите удобную дату:", dateButtons());
            }
            case WAITING_DATE -> {
                userState.setTempDate(text);
                userState.setState(BotState.CONFIRM);
                userStateService.save(userState);
                // Показываем итог и кнопки Подтвердить/Отмена
                String summary = formatSummary(userState);
                bot.sendMessageWithButtons(chatId, summary, confirmButtons());
            }
        }
    }
    
    public void handleCallback(Long chatId, String callbackData) {
        if (callbackData.equals("CONFIRM")) {
            // Создаём заявку
            applicationService.createApplication(chatId);
            userStateService.resetState(chatId);
            bot.sendMessage(chatId, "Заявка принята! Мы свяжемся с вами.");
            // Уведомляем администратора
            notificationService.notifyAdmin(chatId);
        } else if (callbackData.equals("CANCEL")) {
            userStateService.resetState(chatId);
            bot.sendMessage(chatId, "Запись отменена. Напишите /start чтобы начать заново.");
        }
    }
}
```

### Inline кнопки — как создавать
```java
private InlineKeyboardMarkup confirmButtons() {
    InlineKeyboardButton confirmBtn = InlineKeyboardButton.builder()
        .text("Подтвердить")
        .callbackData("CONFIRM")
        .build();
    
    InlineKeyboardButton cancelBtn = InlineKeyboardButton.builder()
        .text("Отмена")
        .callbackData("CANCEL")
        .build();
    
    return InlineKeyboardMarkup.builder()
        .keyboardRow(List.of(confirmBtn, cancelBtn))
        .build();
}
```

### NotificationService — уведомление администратора
```java
@Service
public class NotificationService {
    
    @Value("${telegram.admin.chatId}")
    private Long adminChatId;
    
    public void notifyAdmin(Application app) {
        String message = String.format(
            "Новая заявка!\nИмя: %s\nТелефон: %s\nДата: %s",
            app.getClientName(), app.getPhone(), app.getDesiredDate()
        );
        bot.sendMessage(adminChatId, message);
    }
}
```

---

## Конфигурация бота

### application.yml
```yaml
telegram:
  bot:
    token: ${TELEGRAM_BOT_TOKEN}   # берём из env переменной, не хардкодим!
    username: my_booking_bot
  admin:
    chatId: ${TELEGRAM_ADMIN_CHAT_ID}

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/botdb
```

### BotConfig
```java
@Configuration
public class BotConfig {
    
    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(token, username, ...);
    }
    
    @Bean
    public TelegramBotsApi botsApi(TelegramBot bot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }
}
```

---

## pom.xml зависимости

```xml
<dependency>
    <groupId>org.telegram</groupId>
    <artifactId>telegrambots-spring-boot-starter</artifactId>
    <version>6.9.7.1</version>
</dependency>
<!-- + Spring Boot Web, JPA, PostgreSQL, Lombok как обычно -->
```

---

## Фазы реализации

### Фаза 1 — Скелет бота (30 мин)
- Spring Initializr: Web, JPA, PostgreSQL, Lombok
- Добавить telegrambots-spring-boot-starter
- Создать `TelegramBot` класс, зарегистрировать через `BotConfig`
- Бот должен отвечать "Привет!" на любое сообщение — проверяем, что работает

### Фаза 2 — FSM и состояния в БД (45 мин)
- Создать `BotState` enum
- Создать `UserState` entity и `UserStateRepository`
- `UserStateService.getOrCreate(chatId)` — достать или создать состояние
- `UserStateService.resetState(chatId)` — вернуть в IDLE

### Фаза 3 — Обработчики команд и сообщений (45 мин)
- `CommandHandler.handle(/start)` — установить WAITING_NAME, спросить имя
- `CommandHandler.handle(/cancel)` — сбросить в IDLE
- `MessageHandler.handle()` — делегировать в `BotService`
- `BotService.handleMessage()` — switch по состоянию

### Фаза 4 — Inline кнопки и callbacks (30 мин)
- Для выбора даты — кнопки (несколько вариантов) или свободный ввод
- Кнопки "Подтвердить" / "Отмена" на этапе CONFIRM
- `CallbackHandler` → `BotService.handleCallback()`

### Фаза 5 — Application entity и уведомление (30 мин)
- При CONFIRM — создать `Application`, сохранить в БД
- `NotificationService.notifyAdmin()` — отправить сообщение администратору
- Сбросить состояние пользователя в IDLE

### Фаза 6 — Docker и README (30 мин)
- `docker-compose.yml` с PostgreSQL
- Env переменные для токена и adminChatId
- README: как получить токен у BotFather, как запустить

---

## Что ты умеешь объяснить после этого проекта

- Что такое FSM и почему это правильный подход для ботов
- Почему состояния в БД, а не в памяти (персистентность)
- Как работает Telegram Long Polling (бот сам спрашивает сервер)
- Что такое Callback Query (нажатие на кнопку)
- Почему токен бота в env переменной, а не в коде (безопасность)
- В чём преимущество Java бота перед Python-ботом (типизация, Spring IoC, масштаб)

---

## Ветка в git

```bash
git checkout main
git checkout -b project/2-tg-bot
```
