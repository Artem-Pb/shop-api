package com.polybezev.currencybot.formatter;

public final class BotMessages {
    private BotMessages() {}

    public static final String HELP_TEXT =
            "📋 Что я умею:\n\n" +
            "/curse [КОД] — курс валюты по ЦБ РФ\n" +
            "  Пример: /curse USD\n\n" +
            "/convert [сумма] [ИЗ] [В] — конвертация\n" +
            "  Пример: /convert 100 USD RUB\n\n" +
            "/list — полный список валют ЦБ РФ\n\n" +
            "/btc — курс биткоина в ₽ и $\n\n" +
            "/tier — тиры подписки (скоро 🔜)\n\n" +
            "Быстрый способ: просто напиши код валюты — USD, EUR, CNY, GBP — и я отвечу без команды.\n\n" +
            "Кнопки меню внизу — самые частые действия в один тап 👇";

    public static final String UNKNOWN_INPUT =
            "🤔 Не совсем понял — попробуй иначе:\n\n" +
            "• Напиши код валюты: USD, EUR, CNY\n" +
            "• Или команду: /curse USD, /convert 100 USD RUB\n" +
            "• Полный список команд: /help\n\n" +
            "Кнопки меню тоже работают 👇";

    public static final String UNKNOWN_COMMAND =
            "Такой команды нет. Список всех команд: /help";

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

    public static final String BTC_ERROR =
            "⚠️ Не удалось загрузить курс биткоина — CoinGecko сейчас недоступен.\n" +
            "Попробуйте через пару минут: /btc";

    public static final String LIST_ERROR =
            "⚠️ Не удалось загрузить список валют — ЦБ РФ сейчас не отвечает.\n" +
            "Попробуйте через минуту: /list";
}
