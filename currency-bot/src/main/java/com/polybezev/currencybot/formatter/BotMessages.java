package com.polybezev.currencybot.formatter;

public final class BotMessages {
    private BotMessages() {}

    // ==================== ПРИВЕТСТВИЕ ====================

    // {name} — имя пользователя из Telegram
    public static final String START_TEXT =
            "👋 Привет, {name}!\n\n" +
            "Слежу за курсами валют и крипты — быстро и по делу.\n\n" +
            "Бесплатно:\n" +
            "• /curse USD — курс по ЦБ РФ\n" +
            "• /convert 100 USD RUB — конвертер\n" +
            "• /btc — биткоин в ₽ и $\n" +
            "• /list — все валюты\n\n" +
            "Или просто напиши код: USD, EUR, CNY 👇\n\n" +
            "AI-прогнозы и сигналы → /tier";

    // ==================== НАВИГАЦИЯ ====================

    public static final String HELP_TEXT =
            "📋 Команды:\n\n" +
            "/curse [КОД] — курс валюты по ЦБ РФ\n" +
            "  Пример: /curse USD\n\n" +
            "/convert [сумма] [ИЗ] [В] — конвертер\n" +
            "  Пример: /convert 100 USD RUB\n\n" +
            "/list — все валюты ЦБ РФ\n\n" +
            "/btc — курс биткоина в ₽ и $\n\n" +
            "/tier — подписки и возможности\n\n" +
            "Быстрый способ: напиши код валюты — USD, EUR, CNY, GBP — без команды.\n\n" +
            "Кнопки меню внизу — самые частые действия в один тап 👇";

    public static final String CURSE_KEYBOARD_PROMPT = "Выберите валюту:";

    // ==================== ОШИБКИ И ПОДСКАЗКИ ====================

    public static final String UNKNOWN_INPUT =
            "🤔 Не понял. Напиши код валюты: USD, EUR, CNY — или выбери команду в меню 👇";

    public static final String UNKNOWN_COMMAND =
            "Такой команды нет. Список всех команд: /help";

    // {code} — код, введённый пользователем
    public static final String CURRENCY_NOT_FOUND =
            "🔍 Валюта {code} не найдена.\n\n" +
            "Коды пишутся латиницей: USD, EUR, CNY.\n" +
            "Полный список: /list";

    // ==================== КОНВЕРТЕР ====================

    public static final String CONVERT_FORMAT_ERROR =
            "Формат: /convert 100 USD RUB";

    public static final String CONVERT_AMOUNT_ERROR =
            "Сумма должна быть числом. Пример: /convert 100 USD RUB";

    public static final String CONVERT_AWAIT_AMOUNT =
            "Введите сумму для конвертации:\nНапример: 100, 1500, 0.5";

    public static final String CONVERT_AMOUNT_INVALID =
            "Нужно число — например, 100 или 1500.5\nПопробуйте ещё раз:";

    public static final String CONVERT_AWAIT_FROM =
            "Из какой валюты конвертируем?";

    public static final String CONVERT_AWAIT_TO =
            "В какую валюту переводим?";

    public static final String CONVERT_ERROR =
            "⚠️ Не удалось получить курс — попробуйте чуть позже.\n" +
            "Или воспользуйтесь командой напрямую: /convert 100 USD RUB";

    public static final String CONVERT_FSM_ERROR =
            "Что-то пошло не так. Начните заново — /convert";

    // ==================== ОШИБКИ API ====================

    public static final String BTC_ERROR =
            "⚠️ Не удалось загрузить курс биткоина — CoinGecko сейчас недоступен.\n" +
            "Попробуйте через пару минут: /btc";

    public static final String LIST_ERROR =
            "⚠️ Не удалось загрузить список валют — ЦБ РФ сейчас не отвечает.\n" +
            "Попробуйте через минуту: /list";

    // ==================== ПОДПИСКА ====================

    // {currentTierLabel} — название текущего тира пользователя
    public static final String TIER_CARD =
            "⭐ Ваша подписка: {currentTierLabel}\n\n" +
            "🆓 FREE\n" +
            "• Курсы валют по ЦБ РФ\n" +
            "• Конвертер валют\n" +
            "• Курс биткоина в ₽ и $\n\n" +
            "⚡ TIER 1 — AI-прогнозы\n" +
            "• AI-прогноз по валютной паре\n" +
            "• Утренняя сводка курсов\n" +
            "💳 Telegram Stars\n\n" +
            "📈 TIER 2 — Торговые сигналы\n" +
            "• Технический анализ: RSI, MACD\n" +
            "• Сигналы buy/sell по активам\n" +
            "💳 YooKassa / Stripe\n\n" +
            "🤖 TIER 3 — Автоторговля\n" +
            "• Автоматические сделки на бирже\n" +
            "• Безопасное хранение API-ключей\n" +
            "💳 По запросу";

    // {requiredTier} — нужный тир (пример: TIER 1)
    public static final String ACCESS_DENIED =
            "🔒 Это доступно в {requiredTier}. Подробнее: /tier";

    // ==================== ОПЛАТА — TELEGRAM STARS ====================

    // {tierName} — название тира (пример: TIER 1 — AI-прогнозы)
    public static final String STARS_SUCCESS =
            "✅ Оплата прошла — добро пожаловать в {tierName}!\n\n" +
            "Новые функции уже доступны. Посмотреть подписку: /tier";

    public static final String STARS_ERROR =
            "Оплата не прошла. Попробуйте ещё раз или вернитесь позже: /tier";

    // ==================== УТРЕННЯЯ СВОДКА (TIER 1+) ====================

    // {marketData} — рыночный снимок, {news} — заголовки новостей
    public static final String DIGEST_FALLBACK =
            "📊 Утренняя сводка\n\n" +
            "🏦 Рынок:\n{marketData}\n\n" +
            "📰 Новости:\n{news}\n\n" +
            "⚠️ AI-анализ временно недоступен.";

    // Переменные: {date}, {usdRate}, {usdChange}, {eurRate}, {eurChange},
    //             {cnyRate}, {cnyChange}, {btcRub}, {btcUsd}, {btcChange}
    public static final String MORNING_DIGEST =
            "📊 Сводка на {date}\n\n" +
            "🇺🇸 USD — {usdRate} ₽  {usdChange}\n" +
            "🇪🇺 EUR — {eurRate} ₽  {eurChange}\n" +
            "🇨🇳 CNY — {cnyRate} ₽  {cnyChange}\n\n" +
            "₿ BTC — {btcRub} ₽  /  {btcUsd} $  {btcChange}\n\n" +
            "_Курсы ЦБ РФ и CoinGecko_";

    // ==================== AI-ПРОГНОЗ (TIER 1+) ====================

    // Переменные: {pair}, {direction}, {confidence}, {reasoning}, {date}
    // Требует ParseMode.MARKDOWN при отправке
    public static final String AI_PREDICTION =
            "🤖 AI-прогноз: *{pair}*\n\n" +
            "Направление: {direction}\n" +
            "Уверенность: {confidence}\n\n" +
            "{reasoning}\n\n" +
            "_Дата: {date}_\n" +
            "_⚠️ Не является финансовым советом. Используйте как один из источников анализа._";

    // ==================== TA-СИГНАЛ (TIER 2+) ====================

    public static final String SIGNAL_TIER_REQUIRED =
            "📊 Торговые сигналы доступны с TIER 2 (500 ⭐).\nИспользуй /tier для покупки.";

    // Переменные: {asset}, {signal}, {rsi}, {macd}, {macdSignal}, {date}
    // Требует ParseMode.MARKDOWN при отправке
    public static final String TA_SIGNAL =
            "📈 Сигнал: *{asset}*\n\n" +
            "Итог: *{signal}*\n" +
            "RSI(14): `{rsi}`\n" +
            "MACD: `{macd}`\n" +
            "Сигн. линия: `{macdSignal}`\n\n" +
            "_Дата: {date}_\n" +
            "_⚠️ Не является финансовым советом._";
}
