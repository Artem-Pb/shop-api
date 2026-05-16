package com.polybezev.currencybot.formatter;

public final class BotMessages {
    private BotMessages() {}

    // ==================== ПРИВЕТСТВИЕ ====================

    // {name} — имя пользователя из Telegram
    // Используется и при /start, и при первом входе нового пользователя
    public static final String START_TEXT =
            "👋 Привет, {name}!\n\n" +
            "Я — Currency Bot. Держу тебя в курсе рынка: курсы валют, " +
            "крипта, торговые сигналы и AI-аналитика — всё через кнопки.\n\n" +
            "━━━━━━━━━━━━━━━\n" +
            "🆓  Бесплатно\n" +
            "━━━━━━━━━━━━━━━\n" +
            "📊  Курсы 54 валют по ЦБ РФ — в один тап\n" +
            "💱  Конвертер: любая пара, в том числе BTC ↔ RUB\n" +
            "₿   Bitcoin в ₽ и $, изменение за 24ч\n\n" +
            "━━━━━━━━━━━━━━━\n" +
            "💎  Подписки (Telegram Stars)\n" +
            "━━━━━━━━━━━━━━━\n" +
            "⚡ TIER 1 · 200 ⭐\n" +
            "   Крипто-новости по запросу\n" +
            "   AI-сводка рынка каждое утро в 8:00 МСК\n\n" +
            "📈 TIER 2 · 600 ⭐\n" +
            "   Торговые сигналы RSI / MACD\n" +
            "   AI объясняет каждый сигнал простым языком\n" +
            "   Включает TIER 1\n\n" +
            "🤖 TIER 3 · 1500 ⭐\n" +
            "   Автоторговля через твой биржевой аккаунт\n" +
            "   Включает TIER 1 и TIER 2\n\n" +
            "━━━━━━━━━━━━━━━\n" +
            "Нажми 💎 Подписка — выбери свой уровень.\n" +
            "Или сразу пользуйся бесплатным 👇";

    // Алиас — используется в buildWelcomeNewText() при первом входе
    public static final String WELCOME_NEW = START_TEXT;

    // ==================== НАВИГАЦИЯ ====================

    public static final String HELP_TEXT =
            "📋 Как пользоваться ботом:\n\n" +
            "📊 Курсы — список всех валют ЦБ РФ + быстрый выбор\n" +
            "Конвертер — перевод любой суммы X → Y\n" +
            "₿ BTC — курс биткоина в ₽ и $\n" +
            "📈 Сигналы — RSI/MACD по крипте (TIER 2)\n" +
            "💎 Подписка — тиры и возможности\n" +
            "👤 ЛК — личный кабинет\n\n" +
            "Просто набери код валюты: USD, EUR, CNY, GBP — без команд.\n\n" +
            "Поддержка: напиши в ЛК → Помощь 👇";

    public static final String CURSE_KEYBOARD_PROMPT = "Выберите валюту:";

    // ==================== ОШИБКИ И ПОДСКАЗКИ ====================

    public static final String UNKNOWN_INPUT =
            "🤔 Не понял. Напиши код валюты: USD, EUR, CNY — или выбери действие в меню 👇";

    public static final String UNKNOWN_COMMAND =
            "Такой команды нет. Используй кнопки меню или нажми ❓ Помощь";

    // {code} — код, введённый пользователем
    public static final String CURRENCY_NOT_FOUND =
            "🔍 Валюта {code} не найдена.\n\n" +
            "Коды пишутся латиницей: USD, EUR, CNY.\n" +
            "Полный список — нажми 📊 Курсы";

    // ==================== КОНВЕРТЕР ====================

    public static final String CONVERT_FORMAT_ERROR =
            "Формат: сумма, затем FROM и TO.\nПример: 100 USD RUB";

    public static final String CONVERT_AMOUNT_ERROR =
            "Сумма должна быть числом. Пример: 100 USD RUB";

    public static final String CONVERT_AWAIT_AMOUNT =
            "💱 Конвертер\n\nВведите сумму:\nНапример: 100, 1500, 0.5";

    public static final String CONVERT_AMOUNT_INVALID =
            "💱 Конвертер\n\nНужно число — например, 100 или 1500.5\nПопробуйте ещё раз:";

    public static final String CONVERT_AWAIT_FROM =
            "💱 Конвертер\n\nИз какой валюты конвертируем?\nВыберите или введите код:";

    public static final String CONVERT_AWAIT_TO =
            "💱 Конвертер\n\nВ какую валюту переводим?\nВыберите или введите код:";

    public static final String CONVERT_ERROR =
            "⚠️ Не удалось получить курс — попробуйте чуть позже.\n" +
            "Или запустите конвертер заново через меню.";

    public static final String CONVERT_FSM_ERROR =
            "Что-то пошло не так. Начните заново — нажмите Конвертер";

    // ==================== ОШИБКИ API ====================

    public static final String BTC_ERROR =
            "⚠️ Не удалось загрузить курс биткоина — CoinGecko сейчас недоступен.\n" +
            "Попробуйте через пару минут.";

    public static final String LIST_ERROR =
            "⚠️ Не удалось загрузить список валют — ЦБ РФ сейчас не отвечает.\n" +
            "Попробуйте через минуту.";

    // ==================== ПОДПИСКА ====================

    // {currentTierLabel} — название текущего тира пользователя
    public static final String TIER_CARD =
            "⭐ Ваша подписка: {currentTierLabel}\n\n" +
            "🆓 FREE — бесплатно\n" +
            "• Курсы валют по ЦБ РФ\n" +
            "• Конвертер валют\n" +
            "• Курс биткоина в ₽ и $\n\n" +
            "⚡ TIER 1 — AI-прогнозы\n" +
            "• Новости по запросу (топ крипто)\n" +
            "• Утренняя AI-сводка рынка (8:00 МСК)\n" +
            "• Включает всё из FREE\n\n" +
            "📈 TIER 2 — Торговые сигналы\n" +
            "• TA-сигналы RSI/MACD + AI-объяснение\n" +
            "• Включает всё из TIER 1\n\n" +
            "🤖 TIER 3 — Автоторговля\n" +
            "• Автоматические сделки на бирже\n" +
            "• Безопасное хранение API-ключей\n" +
            "• Включает всё из TIER 2";

    // {requiredTier} — нужный тир (пример: TIER 1)
    public static final String ACCESS_DENIED =
            "🔒 Это доступно в {requiredTier}.\nНажми 💎 Подписка → выбери тир.";

    // ==================== ОПЛАТА — TELEGRAM STARS ====================

    // {tierName} — название тира (пример: TIER 1 — AI-прогнозы)
    public static final String STARS_SUCCESS =
            "✅ Оплата прошла — добро пожаловать в {tierName}!\n\n" +
            "Новые функции уже доступны. Открой 👤 ЛК → посмотри что появилось.";

    public static final String STARS_ERROR =
            "Оплата не прошла. Попробуйте ещё раз через 💎 Подписка.";

    // ==================== ЛИЧНЫЙ КАБИНЕТ ====================

    public static final String LK_HEADER =
            "👤 Личный кабинет\n\nВыбери нужный раздел 👇";

    // {tier} — тир, {expires} — дата истечения или "—", {paid} — уплачено Stars
    public static final String LK_BALANCE =
            "💰 Баланс\n\n" +
            "Активная подписка: {tier}\n" +
            "Действует до: {expires}\n" +
            "Уплачено: {paid} ⭐\n\n" +
            "Для продления или смены тира нажми ⭐ Подписка";

    public static final String LK_HELP =
            "❓ Помощь\n\n" +
            "По всем вопросам пишите: @your_support_username\n\n" +
            "Или опишите проблему — и мы ответим в течение суток.";

    // ==================== НОВОСТИ (TIER 1+) ====================

    public static final String NEWS_HEADER = "📰 Топ новости по крипте:\n\n";

    public static final String NEWS_TIER_REQUIRED =
            "📰 Новости по запросу доступны с TIER 1.\nНажми 💎 Подписка";

    public static final String NEWS_ERROR =
            "⚠️ Не удалось загрузить новости — попробуй позже.";

    // ==================== УТРЕННЯЯ СВОДКА (TIER 1+) ====================

    // {marketData} — рыночный снимок, {news} — заголовки новостей
    public static final String DIGEST_FALLBACK =
            "📊 Утренняя сводка\n\n" +
            "🏦 Рынок:\n{marketData}\n\n" +
            "📰 Новости:\n{news}\n\n" +
            "⚠️ AI-анализ временно недоступен.";

    public static final String DIGEST_TIER_REQUIRED =
            "📊 AI-сводка доступна с TIER 2.\nНажми 💎 Подписка";

    // ==================== AI-ПРОГНОЗ ====================

    // Переменные: {pair}, {direction}, {confidence}, {reasoning}, {date}
    // Требует ParseMode.MARKDOWN при отправке
    public static final String AI_PREDICTION =
            "🤖 AI-прогноз: *{pair}*\n\n" +
            "Направление: {direction}\n" +
            "Уверенность: {confidence}\n\n" +
            "{reasoning}\n\n" +
            "_Дата: {date}_\n" +
            "_⚠️ Не является финансовым советом._";

    // ==================== TA-СИГНАЛ (TIER 2+) ====================

    public static final String SIGNAL_TIER_REQUIRED =
            "📊 Торговые сигналы доступны с TIER 2.\nНажми 💎 Подписка";

    public static final String SIGNAL_PROMPT =
            "📈 Выбери монету для анализа:\n\nRSI(14) + MACD · данные CoinGecko · TIER 2";

    public static final String SIGNAL_UNKNOWN_COIN =
            "❓ Монета не поддерживается.\nДоступны: BTC, ETH, SOL, BNB, XRP";

    // Переменные: {asset}, {signal}, {rsi}, {macd}, {macdSignal}, {aiExplanation}, {date}
    // Требует ParseMode.MARKDOWN при отправке
    public static final String TA_SIGNAL =
            "📈 Сигнал: *{asset}*\n\n" +
            "Итог: *{signal}*\n" +
            "RSI(14): `{rsi}`\n" +
            "MACD: `{macd}`\n" +
            "Сигн. линия: `{macdSignal}`\n\n" +
            "{aiExplanation}\n\n" +
            "_Дата: {date}_\n" +
            "_⚠️ Не является финансовым советом._";

    // ==================== АДМИН-ПАНЕЛЬ ====================

    public static final String ADMIN_HEADER =
            "🔧 Административная панель\n\nВыбери действие:";

    public static final String ADMIN_GRANT_ASK_ID =
            "🔑 Выдать доступ\n\nВведи Telegram chatId пользователя:";

    public static final String ADMIN_GRANT_ASK_TIER =
            "🔑 Выдать доступ\n\nПользователь: {targetId}\n\nВыбери тир:";

    // {targetId}, {tier}
    public static final String ADMIN_GRANT_SUCCESS =
            "✅ {tier} выдан пользователю {targetId}";

    public static final String ADMIN_GRANT_ID_ERROR =
            "❌ chatId должен быть числом. Попробуй снова:";

    public static final String ADMIN_BAN_SUCCESS =
            "🚫 Пользователь {chatId} заблокирован";

    public static final String ADMIN_UNBAN_SUCCESS =
            "✅ Пользователь {chatId} разблокирован";

    // {page}, {total}, {list}
    public static final String ADMIN_USER_LIST =
            "👥 Пользователи (стр. {page}/{total}):\n\n{list}\n\nНажми кнопки для управления";
}
