# Portfolio AI — Обзор системы

## Назначение папки ai/

Здесь хранятся детальные планы реализации каждого проекта.
Перед стартом каждого проекта — читаешь MISSION.md соответствующей папки.
Там: что строим, почему такие решения, как объяснить клиенту, фазы реализации.

## Структура

```
ai/
├── OVERVIEW.md              ← этот файл
├── MY_ROLE.md               ← роли в тандеме, принципы работы
├── SEQUENCE.md              ← порядок проектов, расписание на 4 дня
├── project-1-shop/          ← REST API магазина (ПРИОРИТЕТ 1)
├── project-4-jwt/           ← JWT авторизация (ПРИОРИТЕТ 2)
├── project-2-tg-bot/        ← Telegram FSM бот (ПРИОРИТЕТ 3)
├── project-8-scheduler/     ← Scheduler + парсер (4)
├── project-5-task-manager/  ← Task Manager (5)
├── project-3-api-integration/ ← Интеграция с HH.ru (6)
├── project-9-websocket/     ← WebSocket чат (7)
├── project-6-tg-payment/    ← Telegram Payments (8)
├── project-7-microservices/ ← Два сервиса + Docker (9)
└── project-10-tg-taskmanager/ ← Финальный флагман (10)
```

## Git-структура проектов

Каждый проект — отдельная ветка, отдельный GitHub репозиторий.

```bash
# Старт нового проекта
git checkout main
git checkout -b project/N-name

# Работаем, коммитим по смыслу (не "fix", не "update")
git add src/main/java/.../entity/Product.java
git commit -m "feat: add Product entity with JPA annotations"

# Ты пушишь сам:
git push origin project/N-name
```

## Правило коммитов (Conventional Commits)

```
feat:  — новая функциональность
fix:   — исправление бага
docs:  — README, комментарии
refactor: — переработка без изменения поведения
test:  — тесты
chore: — pom.xml, docker, конфиги
```

## Стек единый для всех проектов

- Java 17
- Spring Boot 3.x
- Spring Data JPA + Hibernate
- PostgreSQL 15
- Docker + Docker Compose
- Lombok (убирает boilerplate)
- Maven

Добавляется по необходимости:
- Spring Security + JWT (проект #4+)
- WebClient (проект #3, #8)
- TelegramBots API (проект #2, #6, #10)
- Spring WebSocket (проект #9)
