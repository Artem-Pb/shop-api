# Миссия #5 — Task Manager REST API

## Что строим

Backend для планировщика задач: создание задач, фильтрация, пагинация, валидация.
Это проект-витрина чистой архитектуры — у него простой домен, но образцовый код.

**Что ты скажешь клиенту:** "Реализовал backend планировщика задач. Фильтрация по статусу и приоритету, пагинация, централизованная обработка ошибок, валидация входных данных."

## Ключевые концепции

### Task entity
```java
@Entity
public class Task {
    @Id @GeneratedValue Long id;
    String title;
    String description;
    @Enumerated(EnumType.STRING) TaskStatus status;   // TODO, IN_PROGRESS, DONE
    @Enumerated(EnumType.STRING) Priority priority;   // LOW, MEDIUM, HIGH
    LocalDate deadline;
    Long userId;  // кому принадлежит задача
}
```

### Пагинация через Pageable — главная фишка
```java
// Controller
@GetMapping
public Page<TaskDto> getTasks(
    @RequestParam(required = false) TaskStatus status,
    @RequestParam(required = false) Priority priority,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "deadline") String sortBy
) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
    return taskService.findAll(status, priority, pageable);
}
```

Ответ клиенту:
```json
{
  "content": [...],
  "totalElements": 42,
  "totalPages": 5,
  "number": 0,
  "size": 10
}
```

### Валидация через @Valid
```java
public class CreateTaskRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100)
    String title;
    
    @NotNull
    Priority priority;
    
    @FutureOrPresent
    LocalDate deadline;
}
```

### GlobalExceptionHandler — обработка ошибок
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        // Собираем все ошибки валидации в один JSON
        return ex.getBindingResult().getFieldErrors().stream()
            .collect(toMap(FieldError::getField, FieldError::getDefaultMessage));
    }
}
```

Ответ при ошибке:
```json
{
  "title": "Title is required",
  "deadline": "must be a date in the present or in the future"
}
```

## Что объяснять

- `Page<T>` — обёртка Spring Data с метаданными пагинации
- `@Valid` + `BindingResult` — декларативная валидация без if-else
- `@ControllerAdvice` — AOP-паттерн для перехвата исключений
- Зачем enum для статуса вместо строки (безопасность типов)

## Ветка

```bash
git checkout -b project/5-task-manager
```
