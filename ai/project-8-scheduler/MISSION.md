# Миссия #8 — Spring Scheduler: автоматический сбор данных

## Что строим

Сервис, который каждые 30 минут автоматически забирает курсы валют с API ЦБ РФ, сохраняет в PostgreSQL и отдаёт историю через REST.

**Что ты скажешь клиенту:** "Реализовал фоновый сервис сбора данных по расписанию. Автоматически забирает данные с внешнего API, сохраняет историю в БД. Применимо для парсинга, мониторинга, отчётности."

## Ключевые компоненты

### @Scheduled — аннотация планировщика
```java
@Service
public class CurrencyScheduler {
    
    @Scheduled(fixedRate = 1800000)  // каждые 30 минут
    // или @Scheduled(cron = "0 0 * * * *")  // каждый час в 00 минут
    public void fetchCurrencyRates() {
        // Забираем данные с ЦБ РФ
        // Сохраняем в БД
    }
}
```

### WebClient — HTTP клиент для внешнего API
```java
@Service
public class CbrApiClient {
    private final WebClient webClient = WebClient.create("https://www.cbr-xml-daily.ru");
    
    public CurrencyResponse fetchRates() {
        return webClient.get()
            .uri("/daily_json.js")  // публичный API, не нужен ключ
            .retrieve()
            .bodyToMono(CurrencyResponse.class)  // десериализация JSON
            .block();
    }
}
```

### Entity — история курсов
```java
@Entity
public class CurrencyRate {
    @Id @GeneratedValue Long id;
    String currencyCode;   // USD, EUR, CNY
    String name;
    BigDecimal value;      // курс в рублях
    LocalDateTime fetchedAt;
}
```

### REST эндпоинт — отдать историю
```
GET /api/rates?currency=USD&from=2024-01-01&to=2024-01-31
```

## Что объяснять

- `@Scheduled` — это Spring-обёртка над Java `ScheduledExecutorService`
- `fixedRate` vs `cron` — разница в логике расписания
- `WebClient` — неблокирующий HTTP клиент (лучше `RestTemplate`)
- Зачем хранить историю, а не только последнее значение

## Ветка

```bash
git checkout -b project/8-scheduler
```
