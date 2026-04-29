# Миссия #1 — REST API интернет-магазина

## Что ты строишь и зачем это уметь объяснить

Ты строишь **серверную часть интернет-магазина** — то, что работает "за кулисами" любого сайта с товарами.
Когда пользователь открывает магазин, нажимает "добавить в корзину", оформляет заказ — всё это идёт к твоему API.
Фронтенд (сайт или мобилка) только отображает то, что ты отдаёшь.

**Что ты скажешь клиенту:** "Я реализовал backend для интернет-магазина: каталог товаров с категориями, корзина, оформление заказа. Всё через REST API, задокументировано в Swagger, запускается одной командой через Docker."

---

## Архитектура: как устроен Spring Boot проект

```
Запрос от клиента
        ↓
[Controller] — принимает HTTP запрос, отдаёт HTTP ответ
        ↓
[Service]    — бизнес-логика (правила: нельзя заказать 0 товаров)
        ↓
[Repository] — работа с базой данных через JPA
        ↓
[Database]   — PostgreSQL хранит данные
```

Это **слоёная архитектура**. Каждый слой знает только о следующем. Controller не лезет в БД. Repository не знает про HTTP. Это стандарт в любом Java-проекте.

---

## Структура пакетов (что и где лежит)

```
src/main/java/com/polybezev/shop/
├── ShopApplication.java          ← точка входа, @SpringBootApplication
│
├── entity/                       ← таблицы в БД, Java объекты
│   ├── Product.java
│   ├── Category.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   └── OrderItem.java
│
├── repository/                   ← интерфейсы для работы с БД
│   ├── ProductRepository.java
│   ├── CategoryRepository.java
│   ├── CartRepository.java
│   └── OrderRepository.java
│
├── service/                      ← бизнес-логика
│   ├── ProductService.java
│   ├── CategoryService.java
│   ├── CartService.java
│   └── OrderService.java
│
├── controller/                   ← HTTP эндпоинты
│   ├── ProductController.java
│   ├── CategoryController.java
│   ├── CartController.java
│   └── OrderController.java
│
├── dto/                          ← что отдаём клиенту (не сырые Entity)
│   ├── ProductDto.java
│   ├── CategoryDto.java
│   ├── CartDto.java
│   └── OrderDto.java
│
└── exception/                    ← обработка ошибок
    ├── ResourceNotFoundException.java
    └── GlobalExceptionHandler.java
```

---

## Зачем DTO? Почему не отдавать Entity напрямую?

Entity — это внутренняя структура БД. В ней могут быть поля для служебных нужд, пароли, ленивые коллекции.
DTO (Data Transfer Object) — это то, что видит клиент. Ты контролируешь формат ответа.

**Пример:** Entity `Product` имеет поле `category` (объект). DTO вернёт `categoryId` и `categoryName` — плоско, без вложенных объектов где не нужно.

---

## Сущности и связи между ними

### Product (товар)
```java
@Entity
public class Product {
    @Id @GeneratedValue
    Long id;
    String name;
    String description;
    BigDecimal price;
    Integer stock;           // остаток на складе
    @ManyToOne
    Category category;       // товар принадлежит одной категории
}
```

### Category (категория)
```java
@Entity
public class Category {
    @Id @GeneratedValue
    Long id;
    String name;
    @OneToMany(mappedBy = "category")
    List<Product> products;  // у категории много товаров
}
```

### Cart (корзина) + CartItem (позиция в корзине)
```java
// Cart — одна корзина на сессию/пользователя
// CartItem — конкретный товар в корзине с количеством
@Entity
public class CartItem {
    @ManyToOne Cart cart;
    @ManyToOne Product product;
    Integer quantity;
}
```

### Order (заказ) + OrderItem (позиция заказа)
```java
// При оформлении заказа: Cart → Order
// CartItem → OrderItem (фиксируем цену на момент заказа!)
@Entity
public class OrderItem {
    @ManyToOne Order order;
    String productName;      // копируем, не ссылаемся — цена могла измениться
    BigDecimal price;
    Integer quantity;
}
```

**Важно:** цену в OrderItem мы копируем из Product на момент заказа. Если потом цена изменится, старый заказ не пересчитывается. Это правильное бизнес-решение.

---

## Эндпоинты API

### Каталог товаров
```
GET    /api/products          — список всех товаров (с пагинацией)
GET    /api/products/{id}     — один товар
GET    /api/products?category=1 — товары по категории
POST   /api/products          — создать товар (ADMIN)
PUT    /api/products/{id}     — обновить товар (ADMIN)
DELETE /api/products/{id}     — удалить товар (ADMIN)

GET    /api/categories        — список категорий
POST   /api/categories        — создать категорию
```

### Корзина
```
GET    /api/cart/{cartId}           — содержимое корзины
POST   /api/cart/{cartId}/items     — добавить товар
PUT    /api/cart/{cartId}/items/{itemId} — изменить количество
DELETE /api/cart/{cartId}/items/{itemId} — убрать товар
DELETE /api/cart/{cartId}           — очистить корзину
```

### Заказы
```
POST   /api/orders            — оформить заказ из корзины
GET    /api/orders/{id}       — статус заказа
GET    /api/orders            — история заказов
PUT    /api/orders/{id}/status — сменить статус (ADMIN)
```

---

## Что происходит при оформлении заказа (бизнес-логика)

```
1. Получаем корзину по cartId
2. Проверяем: корзина не пустая
3. Для каждого CartItem проверяем: product.stock >= quantity
4. Создаём Order со статусом PENDING
5. Для каждого CartItem создаём OrderItem (копируем цену)
6. Уменьшаем stock у каждого Product
7. Очищаем корзину
8. Возвращаем Order
```

Весь этот блок — в одной транзакции (`@Transactional`). Если что-то упадёт на шаге 6 — вся операция откатится. Данные не потеряются.

---

## Docker Compose — как запустить одной командой

```yaml
# docker-compose.yml
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: shopdb
      POSTGRES_USER: shop
      POSTGRES_PASSWORD: shop123
    ports:
      - "5432:5432"

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/shopdb
```

**Что ты скажешь:** "Проект запускается командой `docker-compose up`. Поднимается PostgreSQL и сам сервис. Не нужно ничего устанавливать вручную."

---

## Swagger — автодокументация

Swagger автоматически генерирует страницу со всеми эндпоинтами.
Клиент открывает `http://localhost:8080/swagger-ui.html` и видит:
- Все эндпоинты с описанием
- Форматы запросов/ответов
- Кнопку "Try it out" — сразу тестировать

Зависимость: `springdoc-openapi-starter-webmvc-ui`

---

## Фазы реализации

### Фаза 1 — Инициализация (30 мин)
- Spring Initializr: Web, JPA, PostgreSQL, Validation, Lombok
- `application.yml`: настройка datasource, jpa.ddl-auto=update
- Добавить springdoc зависимость в pom.xml

### Фаза 2 — Сущности и БД (1 час)
- Создать все Entity классы с аннотациями JPA
- Описать связи (@OneToMany, @ManyToOne)
- Запустить — Hibernate создаст таблицы автоматически

### Фаза 3 — Repository слой (30 мин)
- Создать интерфейсы extends JpaRepository
- Добавить нужные методы: `findByCategoryId()`, `findByCartId()`
- JpaRepository даёт findById, findAll, save, delete бесплатно

### Фаза 4 — Service слой (1.5 часа)
- ProductService: CRUD + поиск по категории
- CartService: добавить/убрать/изменить количество
- OrderService: оформление заказа с транзакцией и проверками

### Фаза 5 — Controller + DTO (1 час)
- Controller принимает запрос, вызывает Service, возвращает DTO
- Маппинг Entity → DTO вручную или через MapStruct

### Фаза 6 — Обработка ошибок (30 мин)
- `ResourceNotFoundException` — товар/корзина не найдена → 404
- `GlobalExceptionHandler` с `@ControllerAdvice` — централизованно ловим
- JSON ответ: `{ "error": "Product not found", "status": 404 }`

### Фаза 7 — Docker + README (1 час)
- Dockerfile для Spring Boot приложения
- docker-compose.yml
- README.md: что это, как запустить, список эндпоинтов

---

## Что ты умеешь объяснить после этого проекта

- Что такое REST API и зачем он нужен
- Почему Java + Spring Boot, а не Python/Node
- Как работает JPA и зачем ORM
- Что такое транзакция и почему она важна при заказе
- Почему DTO, а не сырая Entity
- Как Docker упрощает деплой
- Зачем Swagger в коммерческом проекте

---

## Ветка в git

```bash
git checkout -b project/1-shop-api
```

Коммиты по смыслу:
```
feat: add Product and Category entities with JPA relations
feat: add ProductRepository and CategoryRepository
feat: implement ProductService with CRUD operations
feat: add ProductController with REST endpoints
feat: add Cart and Order entities
feat: implement CartService with add/remove/update logic
feat: implement OrderService with transactional checkout
feat: add GlobalExceptionHandler for error responses
feat: add Swagger configuration
feat: add Docker Compose with PostgreSQL
docs: add README with setup instructions and endpoints
```
