# Миссия #10 — Telegram Task Manager (финальный флагман)

## Что строим

Telegram-бот для управления задачами команды: менеджер создаёт задачи, назначает исполнителей, исполнители меняют статус. Напоминания по дедлайну. REST API для веб-панели.

**Что ты скажешь клиенту:** "Реализовал корпоративный инструмент управления задачами через Telegram. Ролевая модель, дедлайны, напоминания, REST API. Показывает умение проектировать полноценную бизнес-систему."

## Это финальный проект — в нём всё

| Компонент | Откуда |
|-----------|--------|
| TelegramBot + FSM | Проект #2 |
| JWT авторизация REST API | Проект #4 |
| Spring Scheduler | Проект #8 |
| Валидация и пагинация | Проект #5 |
| Docker Compose | Все проекты |

## Ролевая модель

```
MANAGER  — создаёт задачи, назначает, закрывает
EXECUTOR — видит свои задачи, меняет статус на IN_PROGRESS/DONE
```

## Сущности

```java
@Entity public class TeamUser {
    Long chatId;     // Telegram chat ID
    String username;
    UserRole role;   // MANAGER, EXECUTOR
}

@Entity public class Task {
    @Id @GeneratedValue Long id;
    String title;
    String description;
    @ManyToOne TeamUser assignee;    // кому назначена
    @ManyToOne TeamUser createdBy;   // кто создал
    TaskStatus status;               // NEW, IN_PROGRESS, DONE, OVERDUE
    LocalDate deadline;
    LocalDateTime createdAt;
    boolean reminderSent;            // отправлено ли напоминание
}
```

## Сценарии в боте

### Менеджер создаёт задачу (FSM)
```
/newtask → "Введите название:" → "Введите описание:" →
"Выберите исполнителя:" [кнопки] → "Дедлайн (YYYY-MM-DD):" → Подтверждение → Создать
```

### Исполнитель работает с задачей
```
/mytasks → список задач с кнопками [Взять в работу] [Выполнено]
```

### Напоминание по дедлайну
```java
@Scheduled(cron = "0 9 * * * *")  // каждый день в 9:00
public void sendDeadlineReminders() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    List<Task> tasks = taskRepository.findByDeadlineAndReminderSentFalse(tomorrow);
    tasks.forEach(task -> {
        bot.sendMessage(task.getAssignee().getChatId(),
            "Дедлайн завтра: " + task.getTitle());
        task.setReminderSent(true);
    });
    taskRepository.saveAll(tasks);
}
```

## REST API (для веб-панели)

```
GET    /api/tasks?assigneeId=5&status=NEW
GET    /api/tasks/{id}
POST   /api/tasks
PUT    /api/tasks/{id}/status
GET    /api/users/team
```

## Что объяснять

- Это интеграция всего стека — демонстрирует системное мышление
- FSM для менеджера другой, чем для исполнителя — разные ветки состояний
- `reminderSent` флаг — защита от дубликатов напоминаний
- REST API параллельно Telegram — один сервис, два интерфейса

## Ветка

```bash
git checkout -b project/10-tg-taskmanager
```
