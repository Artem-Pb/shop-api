package com.polybezev.currencybot.formatter;

import com.polybezev.currencybot.model.CryptoPriceModel;
import com.polybezev.currencybot.model.CurrencyListData;
import com.polybezev.currencybot.model.CurrencyListEntry;
import com.polybezev.currencybot.model.CurrencyModel;
import com.polybezev.currencybot.model.Tier;
import com.polybezev.currencybot.util.CurrencyFlags;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class MessageFormatter {

    // ==================== KEYBOARDS ====================

    public InlineKeyboardMarkup buildConvertKeyboard() {
        List<List<InlineKeyboardButton>> rows = baseCurrencyRows();
        rows.add(row(btn("🇷🇺 RUB", "RUB")));
        return markup(rows);
    }

    public InlineKeyboardMarkup buildRatesKeyboard() {
        return markup(baseCurrencyRows());
    }

    public InlineKeyboardMarkup buildTierKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row(btn("⚡ TIER 1 — 150 Stars", "BUY_TIER_1")));
        rows.add(row(btn("📈 TIER 2 — 500 Stars", "BUY_TIER_2")));
        rows.add(row(btn("🤖 TIER 3 — 1500 Stars", "BUY_TIER_3")));
        return markup(rows);
    }

    public ReplyKeyboardMarkup buildMainKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📊 Курсы"));
        row1.add(new KeyboardButton("Конвертер"));
        row1.add(new KeyboardButton("₿ BTC"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("💎 Подписка"));
        row2.add(new KeyboardButton("❓ Помощь"));

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(List.of(row1, row2));
        markup.setResizeKeyboard(true);
        return markup;
    }

    // ==================== TEXTS ====================

    public String buildStartText(String name) {
        return BotMessages.START_TEXT.replace("{name}", name);
    }

    public String buildHelpText() {
        return BotMessages.HELP_TEXT;
    }

    public String buildUnknownInputText() {
        return BotMessages.UNKNOWN_INPUT;
    }

    public String buildCurrencyNotFoundText(String code) {
        return BotMessages.CURRENCY_NOT_FOUND.replace("{code}", code);
    }

    public String buildTierCard(Tier current) {
        return BotMessages.TIER_CARD.replace("{currentTierLabel}", current.label);
    }

    public String buildCryptoCard(CryptoPriceModel model) {
        String arrow = model.getChange24h() >= 0 ? "▲" : "▼";
        return String.format(
                "₿ %s\n\n" +
                "1 %s = %.0f ₽\n" +
                "1 %s = %.0f $\n\n" +
                "%s %.2f%% за 24ч\n\n" +
                "📅 CoinGecko · %s",
                model.getSymbol(),
                model.getSymbol(), model.getPriceRub(),
                model.getSymbol(), model.getPriceUsd(),
                arrow, model.getChange24h(),
                new SimpleDateFormat("dd.MM.yyyy").format(new Date())
        );
    }

    public String buildRateCard(CurrencyModel c) {
        String flag = CurrencyFlags.getFlag(c.getCharCode());
        String nominalPrefix = c.getNominal() > 1
                ? c.getNominal() + " " + c.getCharCode() + " = "
                : "1 " + c.getCharCode() + " = ";

        Double diff = c.getDiff();
        Double percent = c.getPercentChange();

        String changeSymbol   = diff == null ? "" : (diff >= 0 ? "📈" : "📉");
        String formattedDiff  = diff == null ? "—" : String.format("%+.4f", diff);
        String formattedPct   = percent == null ? "—" : String.format("%+.2f%%", percent);

        return String.format(
                "%s %s · %s\n\n" +
                "%s%.2f ₽\n" +
                "%s %s (%s)\n\n" +
                "📅 ЦБ РФ · %s",
                flag, c.getCharCode(), c.getName(),
                nominalPrefix, c.getValue(),
                changeSymbol, formattedDiff, formattedPct,
                formatDate(c.getDate())
        );
    }

    public String buildConvertResult(double amount, String from, double result, String to) {
        String source = (from.equals("BTC") || to.equals("BTC")) ? "CoinGecko" : "ЦБ РФ";
        return "💱 " + formatAmount(amount, from) + " " + from
                + " = " + formatAmount(result, to) + " " + to
                + "\n\nКурс: " + source;
    }

    public String buildCurrencyList(CurrencyListData data) {
        StringBuilder sb = new StringBuilder("Доступные валюты ЦБ РФ:\n\n");
        for (CurrencyListEntry entry : data.currencies()) {
            sb.append(String.format("%s %s — %s\n",
                    CurrencyFlags.getFlag(entry.code()), entry.code(), entry.name()));
        }
        sb.append("\nВсего: ").append(data.currencies().size()).append(" валют");
        sb.append("\nДанные на: ").append(data.feedDate());
        sb.append("\n\n💡 Используйте код валюты для получения курса");
        sb.append("\nНапример: USD или /curse USD");
        return sb.toString();
    }

    // ==================== PRIVATE HELPERS ====================

    public String formatAmount(double value, String currency) {
        return currency.equals("BTC")
                ? String.format("%.10f", value)
                : String.format("%.2f", value);
    }

    private String formatDate(Date date) {
        if (date == null) return "—";
        return new SimpleDateFormat("dd.MM.yyyy").format(date);
    }

    private InlineKeyboardMarkup markup(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private List<List<InlineKeyboardButton>> baseCurrencyRows() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row(btn("🇺🇸 USD", "USD"), btn("🇪🇺 EUR", "EUR"), btn("🇨🇳 CNY", "CNY")));
        rows.add(row(btn("🇬🇧 GBP", "GBP"), btn("🇯🇵 JPY", "JPY"), btn("🇨🇭 CHF", "CHF")));
        rows.add(row(btn("🇹🇷 TRY", "TRY"), btn("🇦🇪 AED", "AED"), btn("🇰🇿 KZT", "KZT")));
        rows.add(row(btn("🇧🇾 BYN", "BYN"), btn("🇨🇦 CAD", "CAD"), btn("🇭🇰 HKD", "HKD")));
        rows.add(row(btn("₿ BTC", "BTC")));
        return rows;
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
}
