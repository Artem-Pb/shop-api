# Миссия #3 — Интеграция с внешним API (HH.ru)

## Что строим

Сервис, который забирает вакансии с публичного API HH.ru, трансформирует, сохраняет в PostgreSQL, и отдаёт через свой REST API с фильтрами.

**Что ты скажешь клиенту:** "Реализовал интеграцию с внешним API. Fetch данных → трансформация → хранение → выдача. Паттерн применим к WB, OZON, AmoCRM, Bitrix — любому REST API."

## Архитектура — поток данных

```
HH.ru API
    ↓ (WebClient — HTTP запрос)
[HhApiClient]       — только общение с внешним API
    ↓ (HhVacancyDto — DTO внешнего API)
[VacancyMapper]     — трансформация в нашу модель
    ↓ (Vacancy entity)
[VacancyRepository] — сохранение в нашу БД
    ↓
[VacancyController] — наш REST API для клиентов
```

## HH.ru API — без токена

```
GET https://api.hh.ru/vacancies?text=java&area=1&per_page=20
```

Возвращает JSON с вакансиями. Публичный, бесплатный.

## Ключевые компоненты

### DTO внешнего API (что приходит с HH.ru)
```java
public class HhVacancyDto {
    String id;
    String name;                    // название вакансии
    HhSalaryDto salary;             // { from, to, currency }
    HhEmployerDto employer;         // { name }
    HhAreaDto area;                 // { name } — город
    String publishedAt;
    String alternateUrl;            // ссылка на вакансию
}
```

### Наша Vacancy entity (что храним у себя)
```java
@Entity
public class Vacancy {
    @Id @GeneratedValue Long id;
    String externalId;         // id с HH.ru (не дублировать)
    String title;
    String company;
    String city;
    Integer salaryFrom;
    Integer salaryTo;
    String currency;
    LocalDateTime publishedAt;
    String url;
    LocalDateTime savedAt;     // когда мы сохранили
}
```

### HhApiClient — WebClient
```java
@Service
public class HhApiClient {
    
    private final WebClient webClient = WebClient.builder()
        .baseUrl("https://api.hh.ru")
        .defaultHeader("User-Agent", "HH-Portfolio-Project/1.0")  // обязательно для HH.ru
        .build();
    
    public HhSearchResponse searchVacancies(String query, int page) {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/vacancies")
                .queryParam("text", query)
                .queryParam("area", "1")        // Москва
                .queryParam("per_page", "20")
                .queryParam("page", page)
                .build())
            .retrieve()
            .bodyToMono(HhSearchResponse.class)
            .block();
    }
}
```

### Наш REST API
```
GET /api/vacancies?query=java&city=Москва&salaryFrom=100000
GET /api/vacancies/{id}
POST /api/vacancies/fetch?query=java  — триггер загрузки с HH.ru
```

## Что объяснять

- Зачем два DTO (внешний и внутренний) — независимость от изменений HH.ru API
- `WebClient` vs `RestTemplate` — реактивность, цепочки операций
- `externalId` — как избежать дублирования при повторной загрузке
- Почему сохраняем копию, а не проксируем запросы — скорость и надёжность

## Ветка

```bash
git checkout -b project/3-api-integration
```
