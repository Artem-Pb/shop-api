package com.polybezev.currencybot.handler;

import com.polybezev.currencybot.formatter.MessageFormatter;
import com.polybezev.currencybot.model.CryptoPriceModel;
import com.polybezev.currencybot.model.CurrencyModel;
import com.polybezev.currencybot.service.CryptoService;
import com.polybezev.currencybot.service.CurrencyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.IOException;

@Component
@AllArgsConstructor
public class CommandHandler {

    private final CurrencyService currencyService;
    private final MessageFormatter formatter;
    private final CryptoService cryptoService;

    // ==================== ENTRY POINTS ====================

    public SendMessage handleCommand(String text, long chatId, String userName) {
        String[] parts = text.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].trim() : "";

        return switch (cmd) {
            case "/start" -> msg(chatId, formatter.buildStartText(userName), formatter.buildCurrencyKeyboard());
            case "/help" -> msg(chatId, formatter.buildHelpText());
            case "/list" -> handleList(chatId);
            case "/curse" -> arg.isEmpty() ? handleList(chatId) : handleCurrencyRequest(arg.toUpperCase(), chatId);
            case "/convert" -> handleConvert(arg, chatId);
            case "/btc" -> handleBtc(chatId);
            default -> msg(chatId, "Неизвестная команда. Используйте /help");
        };
    }

    public SendMessage handleText(String text, long chatId) {
        String upper = text.toUpperCase().trim();

        if (upper.matches("[A-Z]{3}")) return handleCurrencyRequest(upper, chatId);
        if (upper.contains("ДОЛЛАР")) return handleCurrencyRequest("USD", chatId);
        if (upper.contains("ЕВРО")) return handleCurrencyRequest("EUR", chatId);
        if (upper.contains("ЮАНЬ")) return handleCurrencyRequest("CNY", chatId);

        return msg(chatId, formatter.buildUnknownInputText());
    }

    // ==================== FEATURE HANDLERS ====================

    public SendMessage handleCurrencyRequest(String code, long chatId) {
        try {
            CurrencyModel currency = currencyService.getCurrency(code);
            return msg(chatId, formatter.buildRateCard(currency));
        } catch (Exception e) {
            return msg(chatId, formatter.buildCurrencyNotFoundText(code));
        }
    }

    private SendMessage handleList(long chatId) {
        try {
            String list = currencyService.getFormattedCurrencyList();
            return msg(chatId, list, formatter.buildCurrencyKeyboard());
        } catch (IOException e) {
            return msg(chatId, "Не удалось загрузить список валют. Проверьте соединение.");
        }
    }

    public SendMessage handleConvert(String arg, Long chatId) {
        String[] parts = arg.split("\\s+");

        if (parts.length != 3) {
            return msg(chatId, "Формат: /convert 100 USD RUB");
        }

        try {
            double amount = Double.parseDouble(parts[0]);
            String from = parts[1].toUpperCase();
            String to = parts[2].toUpperCase();

            if (from.equals("BTC") || to.equals("BTC")) {
                return handleBtcConvert(chatId, amount, from, to);
            }

            double result = currencyService.convertCurrency(amount, from, to);
            String text = String.format("Вы получите по ЦБ: %.2f %s = %.2f %s", amount, from, result, to);

            return msg(chatId, text);

        } catch (NumberFormatException e) {
            return msg(chatId, "Сумма должна быть числом. Пример: /convert 100 USD RUB");

        } catch (Exception e) {
            return msg(chatId, "❌" + e.getMessage());
        }
    }

    public SendMessage handleBtc(long chatId) {
        try {
            CryptoPriceModel model = cryptoService.getCryptoPrice("bitcoin");
            model.setSymbol("BTC");
            return msg(chatId, formatter.buildCryptoCard(model));
        } catch (IOException e) {
            return msg(chatId, "Не получилось получить цену BTC! Попробуйте позже!");
        }
    }

    private SendMessage handleBtcConvert(long chatId, double amount, String from, String to) {
        try {
            CryptoPriceModel model = cryptoService.getCryptoPrice("bitcoin");
            double result;
            if (to.equals("RUB")) {
                result = amount * model.getPriceRub();
            } else if (from.equals("RUB")) {
                result = amount / model.getPriceRub();
            } else if (to.equals("USD")) {
                result = amount * model.getPriceUsd();
            } else if (from.equals("USD")) {
                result = amount / model.getPriceUsd();
            } else {
                return msg(chatId, "BTC можно конвертировать только в RUB или USD");
            }

            return msg(chatId, formatter.formatAmount(amount, from) + " " + from + " = "
                    + formatter.formatAmount(result, to) + " " + to);
        } catch (Exception e) {
            return msg(chatId, "Не получилось узнать актуальный курс, приходите позже.");
        }
    }

    // ==================== BUILDERS ====================

    private SendMessage msg(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        return message;
    }

    private SendMessage msg(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = msg(chatId, text);
        message.setReplyMarkup(keyboard);
        return message;
    }
}
