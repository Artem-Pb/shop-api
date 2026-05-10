package com.polybezev.currencybot.formatter;

import com.polybezev.currencybot.model.CryptoPriceModel;
import com.polybezev.currencybot.model.CurrencyModel;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class MessageFormatter {

    // ==================== KEYBOARDS ====================

    public InlineKeyboardMarkup buildCurrencyKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row(btn("🇺🇸 USD", "USD"), btn("🇪🇺 EUR", "EUR"), btn("🇨🇳 CNY", "CNY")));
        rows.add(row(btn("🇬🇧 GBP", "GBP"), btn("🇯🇵 JPY", "JPY"), btn("🇨🇭 CHF", "CHF")));
        rows.add(row(btn("🇹🇷 TRY", "TRY"), btn("🇦🇪 AED", "AED"), btn("🇰🇿 KZT", "KZT")));
        rows.add(row(btn("🇧🇾 BYN", "BYN"), btn("🇨🇦 CAD", "CAD"), btn("🇭🇰 HKD", "HKD")));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    // ==================== TEXTS ====================

    public String buildCryptoCard(CryptoPriceModel model) {
        String arrow = model.getChange24h() >= 0 ? "▲" : "▼";

        return String.format(
                """
                        ₿ %s
                        
                        1 %s = %.0f ₽
                        1 %s = %.0f $
                        
                        %s %.2f%% за 24ч
                        
                        \uD83D\uDCC5 CoinGecko · %s""",
                model.getSymbol(),
                model.getSymbol(), model.getPriceRub(),
                model.getSymbol(), model.getPriceUsd(),
                arrow, model.getChange24h(), new SimpleDateFormat("dd.MM.yyyy").format(new Date())
        );
    }

    public String buildStartText(String name) {
        return "👋 Привет, " + name + "!\n\n" +
                "Я покажу курсы ЦБ РФ в реальном времени.\n\n" +
                "📌 Команды:\n" +
                "  /curse USD — курс валюты\n" +
                "  /list — все доступные валюты\n\n" +
                "Или просто напиши код: USD, EUR, CNY";
    }

    public String buildHelpText() {
        return "📋 Команды бота:\n\n" +
                "/curse [CODE] — курс валюты\n" +
                "  Пример: /curse USD\n\n" +
                "/list — все валюты ЦБ РФ\n\n" +
                "Или напиши код напрямую: USD, EUR, CNY...";
    }

    public String buildRateCard(CurrencyModel c) {
        String flag = getCountryFlag(c.getCharCode());
        String nominalPrefix = c.getNominal() > 1
                ? c.getNominal() + " " + c.getCharCode() + " = "
                : "1 " + c.getCharCode() + " = ";

        return String.format(
                "%s %s · %s\n\n" +
                "%s%.2f ₽\n" +
                "%s %s (%s)\n\n" +
                "📅 ЦБ РФ · %s",
                flag, c.getCharCode(), c.getName(),
                nominalPrefix, c.getValue(),
                c.getChangeSymbol(), c.getFormattedDiff(), c.getFormattedPercentChange(),
                formatDate(c.getDate())
        );
    }

    public String buildCurrencyNotFoundText(String code) {
        return "❌ Валюта " + code + " не найдена.\n" +
                "Проверьте код и попробуйте снова.\n\n" +
                "Все доступные валюты: /list";
    }

    public String buildUnknownInputText() {
        return "🤔 Не понял запрос.\n\n" +
                "Попробуйте: USD, EUR, CNY\n" +
                "Или команды: /help, /list";
    }

    // ==================== PRIVATE HELPERS ====================

    private String formatDate(Date date) {
        if (date == null) return "—";
        return new SimpleDateFormat("dd.MM.yyyy").format(date);
    }

    private String getCountryFlag(String code) {
        return switch (code) {
            case "USD" -> "🇺🇸";
            case "EUR" -> "🇪🇺";
            case "GBP" -> "🇬🇧";
            case "JPY" -> "🇯🇵";
            case "CNY" -> "🇨🇳";
            case "CHF" -> "🇨🇭";
            case "CAD" -> "🇨🇦";
            case "AUD" -> "🇦🇺";
            case "NZD" -> "🇳🇿";
            case "TRY" -> "🇹🇷";
            case "AED" -> "🇦🇪";
            case "KZT" -> "🇰🇿";
            case "BYN" -> "🇧🇾";
            case "HKD" -> "🇭🇰";
            default -> "•";
        };
    }

    private InlineKeyboardButton btn(String text, String callbackData) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(callbackData);
        return btn;
    }

    private List<InlineKeyboardButton> row(InlineKeyboardButton... buttons) {
        return new ArrayList<>(Arrays.asList(buttons));
    }

    public String formatAmount(double value, String currency) {
        return currency.equals("BTC")
                ? String.format("%.10f", value)
                : String.format("%.2f", value);
    }
}
