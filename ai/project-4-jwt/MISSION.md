# Миссия #4 — Spring Security: JWT авторизация + роли

## Что ты строишь и зачем это уметь объяснить

Ты добавляешь **систему безопасности** к уже существующему API магазина.
Без авторизации любой может создать/удалить товар. После — только ADMIN может управлять каталогом, USER — только покупать.

JWT (JSON Web Token) — это подписанный токен, который клиент получает при входе и отправляет с каждым запросом.
Сервер проверяет подпись — и знает, кто ты и какие у тебя права. **Без хранения сессий на сервере.**

**Что ты скажешь клиенту:** "Реализовал stateless авторизацию через JWT. Регистрация, вход, защищённые эндпоинты по ролям USER/ADMIN, автоматическое обновление токена через refresh token."

---

## Как работает JWT — просто

```
1. Клиент: POST /auth/login  { email, password }
2. Сервер: проверяет пароль → создаёт токен → отдаёт
3. Клиент: сохраняет токен (localStorage, куки)
4. Клиент: GET /api/products  →  Header: Authorization: Bearer <токен>
5. Сервер: расшифровывает токен, видит user_id и роль → разрешает или 401
```

JWT состоит из трёх частей, разделённых точкой:
- **Header** — алгоритм шифрования (HS256)
- **Payload** — данные: userId, email, role, expiration
- **Signature** — подпись сервера (никто не подделает без секретного ключа)

---

## Что добавляем к проекту #1

### Новые сущности
```
entity/
├── User.java          ← таблица users (id, email, password, role)
└── RefreshToken.java  ← таблица refresh_tokens (id, token, userId, expiresAt)
```

### Новые компоненты Security
```
security/
├── JwtUtil.java              ← генерация и валидация токенов
├── JwtAuthFilter.java        ← фильтр — перехватывает каждый запрос
├── UserDetailsServiceImpl.java ← загружает юзера из БД для Spring Security
└── SecurityConfig.java       ← настройка цепочки безопасности
```

### Новый контроллер
```
controller/
└── AuthController.java       ← /auth/register, /auth/login, /auth/refresh
```

---

## Детальный разбор каждого компонента

### User entity
```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id @GeneratedValue
    Long id;
    String email;
    String password;      // хранится в BCrypt hash, НИКОГДА в открытом виде
    @Enumerated(EnumType.STRING)
    Role role;            // enum: USER, ADMIN
    
    // UserDetails методы: getAuthorities() → возвращает роль как GrantedAuthority
}
```

**Почему implements UserDetails?** Spring Security работает с объектами UserDetails. Реализуя интерфейс прямо в Entity, мы говорим Spring: "вот твой пользователь, используй его".

### JwtUtil — сердце JWT логики
```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")         // секретный ключ из application.yml
    private String secret;
    
    @Value("${jwt.expiration}")     // время жизни access token (15 мин)
    private long expiration;
    
    // Создать токен: берём userId и role, подписываем ключом
    public String generateToken(User user) { ... }
    
    // Проверить токен: подпись верна? не истёк?
    public boolean validateToken(String token) { ... }
    
    // Достать данные из токена
    public String extractEmail(String token) { ... }
    public String extractRole(String token) { ... }
}
```

### JwtAuthFilter — перехватчик запросов
```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(request, response, filterChain) {
        // 1. Достаём заголовок Authorization: Bearer <token>
        String token = extractToken(request);
        
        // 2. Если токен есть и валиден
        if (token != null && jwtUtil.validateToken(token)) {
            // 3. Достаём email из токена
            String email = jwtUtil.extractEmail(token);
            // 4. Загружаем юзера из БД
            UserDetails user = userDetailsService.loadUserByUsername(email);
            // 5. Говорим Spring Security: этот запрос аутентифицирован
            SecurityContextHolder.getContext().setAuthentication(...);
        }
        
        filterChain.doFilter(request, response); // передаём дальше
    }
}
```

**Простыми словами:** каждый запрос проходит через этот фильтр. Если токен правильный — пропускаем с правами. Нет токена — пропускаем без прав (дальше SecurityConfig решит, пустить или 401).

### SecurityConfig — правила доступа
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())           // REST API, не формы
            .sessionManagement(s -> s.stateless())  // без сессий на сервере
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()           // вход/регистрация — всем
                .requestMatchers("/swagger-ui/**").permitAll()     // документация — всем
                .requestMatchers(POST, "/api/products").hasRole("ADMIN")   // создать — только ADMIN
                .requestMatchers(DELETE, "/api/products/**").hasRole("ADMIN")
                .anyRequest().authenticated()                       // остальное — любой авторизованный
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // хэш пароля
    }
}
```

### AuthController — регистрация и вход
```
POST /auth/register
Body: { "email": "user@mail.com", "password": "pass123", "role": "USER" }
Response: { "message": "User registered successfully" }

POST /auth/login
Body: { "email": "user@mail.com", "password": "pass123" }
Response: { "accessToken": "eyJ...", "refreshToken": "uuid-string" }

POST /auth/refresh
Body: { "refreshToken": "uuid-string" }
Response: { "accessToken": "eyJ..." }
```

---

## Refresh Token — зачем нужен

Access Token живёт 15 минут. Если бы не было Refresh Token — пользователь бы вводил пароль каждые 15 минут.

Refresh Token живёт 30 дней, хранится в БД. Когда Access Token истёк:
```
Клиент → POST /auth/refresh { refreshToken }
Сервер → проверяет в БД → выдаёт новый Access Token
```

Refresh Token можно отозвать (logout): удалить из БД → пользователь разлогинен.

---

## Схема потока данных

```
Регистрация:    POST /auth/register → хешируем пароль → сохраняем User → 200 OK
Вход:           POST /auth/login → проверяем BCrypt → генерируем JWT → отдаём
Запрос:         GET /api/cart → JwtAuthFilter → валидация → SecurityContext → Controller
Обновление:     POST /auth/refresh → ищем RefreshToken в БД → новый AccessToken
Выход:          DELETE /auth/logout → удаляем RefreshToken из БД → токен нельзя обновить
```

---

## Зависимости (pom.xml)

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```

---

## Фазы реализации

### Фаза 1 — User и роли (30 мин)
- Создать enum `Role { USER, ADMIN }`
- Создать `User` entity, `RefreshToken` entity
- Создать `UserRepository`

### Фаза 2 — JwtUtil (30 мин)
- Добавить секрет и время жизни в `application.yml`
- Реализовать `generateToken`, `validateToken`, `extractEmail`, `extractRole`
- Протестировать в unit тесте (опционально)

### Фаза 3 — Security компоненты (45 мин)
- `UserDetailsServiceImpl` — загрузка юзера из БД
- `JwtAuthFilter` — перехват и проверка токена
- `SecurityConfig` — правила доступа, добавление фильтра

### Фаза 4 — AuthController (45 мин)
- DTO: `RegisterRequest`, `LoginRequest`, `AuthResponse`
- `AuthService`: register (BCrypt + save), login (BCrypt check + JWT генерация)
- Refresh token логика

### Фаза 5 — Защита эндпоинтов магазина (15 мин)
- Добавить `@PreAuthorize("hasRole('ADMIN')")` на нужные методы
- Включить `@EnableMethodSecurity` в SecurityConfig

### Фаза 6 — Тест в Postman (30 мин)
- Зарегистрировать USER и ADMIN
- Получить токены
- Попробовать запрос к `/api/products` (POST) с USER токеном → 403
- Тот же запрос с ADMIN токеном → 201 Created
- Скриншот всего этого

---

## Что ты умеешь объяснить после этого проекта

- Чем JWT лучше сессий (stateless, масштабируется)
- Почему пароли нельзя хранить в открытом виде (BCrypt)
- Как работает цепочка фильтров Spring Security
- Зачем нужны два токена (access + refresh)
- Что такое 401 vs 403 (не авторизован vs нет прав)

---

## Ветка в git

```bash
git checkout -b project/4-jwt-auth
```

Если строим поверх #1 — форкаем с той ветки:
```bash
git checkout project/1-shop-api
git checkout -b project/4-jwt-auth
```
