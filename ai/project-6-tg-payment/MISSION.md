# Миссия #6 — Telegram-бот с оплатой

## Что строим

Telegram-бот с встроенной оплатой: каталог услуг → выбор → оплата через Telegram Payments → уведомление. Все транзакции в БД.

**Что ты скажешь клиенту:** "Реализовал Telegram-бот для продажи услуг с встроенной оплатой. Telegram Payments API (тестовый режим), хранение транзакций в PostgreSQL."

## Как работает Telegram Payments

```
Бот показывает товар с кнопкой "Купить"
    ↓
Telegram открывает платёжную форму (карта, ApplePay, GooglePay)
    ↓
Telegram отправляет боту pre_checkout_query (подтверди, что готов)
    ↓
Бот подтверждает: answerPreCheckoutQuery(ok=true)
    ↓
Telegram списывает деньги, отправляет боту successful_payment
    ↓
Бот сохраняет транзакцию, отдаёт доступ к услуге
```

## Ключевые методы

```java
// Отправить инвойс (счёт на оплату)
SendInvoice invoice = SendInvoice.builder()
    .chatId(chatId)
    .title("Консультация")
    .description("1 час консультации по праву")
    .payload("consultation-1h")          // наш идентификатор товара
    .providerToken(PAYMENT_TOKEN)         // токен от @BotFather (тест: "TEST")
    .currency("RUB")
    .prices(List.of(new LabeledPrice("Консультация", 150000)))  // в копейках!
    .build();

execute(invoice);
```

```java
// Обработать pre_checkout (обязательно!)
@Override
public void onUpdateReceived(Update update) {
    if (update.hasPreCheckoutQuery()) {
        AnswerPreCheckoutQuery answer = AnswerPreCheckoutQuery.builder()
            .preCheckoutQueryId(update.getPreCheckoutQuery().getId())
            .ok(true)  // подтверждаем, что товар доступен
            .build();
        execute(answer);
    }
    
    if (update.hasMessage() && update.getMessage().hasSuccessfulPayment()) {
        // Платёж прошёл! Сохраняем и отдаём товар
        SuccessfulPayment payment = update.getMessage().getSuccessfulPayment();
        transactionService.save(chatId, payment);
        bot.sendMessage(chatId, "Спасибо! Доступ открыт.");
    }
}
```

## Transaction entity

```java
@Entity
public class PaymentTransaction {
    @Id @GeneratedValue Long id;
    Long chatId;
    String telegramPaymentChargeId;  // ID платежа от Telegram
    String payload;                  // наш идентификатор товара
    Integer totalAmount;             // сумма в копейках
    String currency;
    LocalDateTime paidAt;
}
```

## Что объяснять

- `pre_checkout_query` — почему нельзя пропустить (условие Telegram)
- Суммы в копейках — стандарт API (150000 = 1500.00 RUB)
- `payload` — как связать платёж с конкретным товаром/услугой
- Тестовый режим через @BotFather → провайдер `"TEST"`

## Ветка

```bash
git checkout -b project/6-tg-payment
```
