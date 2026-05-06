package com.polybezev.currencybot.bot;

import com.polybezev.currencybot.util.UserInfoExtractor;
import com.polybezev.currencybot.config.BotConfig;
import com.polybezev.currencybot.model.CurrencyModel;
import com.polybezev.currencybot.service.CurrencyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class CurrencyBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    private final CurrencyService currencyService;

    private static final String usdText = "\uD83C\uDDFA\uD83C\uDDF8 USD";
    private static final String eurText = "\uD83C\uDDEA\uD83C\uDDFA EUR";
    private static final String cnyText = "\uD83C\uDDE8\uD83C\uDDF3 CNY";
    private static final String gbpText = "\uD83C\uDDEC\uD83C\uDDE7 GBP";
    private static final String jpyText = "\uD83C\uDDEF\uD83C\uDDF5 JPY";
    private static final String chfText = "\uD83C\uDDE8\uD83C\uDDED CHF";
    private static final String tryText = "\uD83C\uDDF9\uD83C\uDDF7 TRY";
    private static final String aedText = "\uD83C\uDDE6\uD83C\uDDEA AED";
    private static final String kznText = "\uD83C\uDDF0\uD83C\uDDFF KZT";
    private static final String bynText = "\uD83C\uDDE7\uD83C\uDDFE BYN";
    private static final String cadText = "\uD83C\uDDE8\uD83C\uDDE6 CAD";
    private static final String hkdText = "\uD83C\uDDED\uD83C\uDDF0 HKD";

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        String userName = UserInfoExtractor.getFirstName(update);
        if (userName == null) {
            System.out.println("Пишет " + userName + ": " + messageText);
        }

        if (messageText.startsWith("/")) handleCommand(messageText, chatId, userName);
        else if (messageText.matches("[A-Z]{3}")) handleCurrencyRequest(messageText, chatId);
        else handlePlainText(messageText, chatId);
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());

        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            //TODO exceptionHandler;
        }

        handleCurrencyRequest(callbackData, chatId);
    }

    private void handlePlainText(String text, long chatId) {
        String upperText = text.toUpperCase().trim();

        if (upperText.matches("[A-Z]{3}")) {
            handleCurrencyRequest(upperText, chatId);
        }
        else if (upperText.contains("ДОЛЛАР") || upperText.contains("USD")) {
            handleCurrencyRequest("USD", chatId);
        }
        else if (upperText.contains("ЕВРО") || upperText.contains("EUR")) {
            handleCurrencyRequest("EUR", chatId);
        }
        else if (upperText.contains("ЮАНЬ") || upperText.contains("CNY")) {
            handleCurrencyRequest("CNY", chatId);
        }
        else {
            sendMessage(chatId,
                    "🤔 Я понимаю:\n" +
                            "• Коды валют: USD, EUR, CNY\n" +
                            "• Команды: /start, /help, /list\n\n" +
                            "Попробуйте отправить 'USD' или '/curse EUR'"
            );
        }
    }

    private void handleCommand(String command, long chatId, String userName) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String argument = (parts.length > 1) ? parts[1].trim() : "";

        switch (cmd) {
            case "/start":
                startCommandReceived(chatId, userName);
                break;
            case "/help":
                sendMessage(chatId, "Доступные команды:\n" +
                        "/curse - доступные курсы валют \n" +
                        "/list - все валюты\n" +
                        "Или введите интересующий вас курс валют в формате: USD, EUR и тд.");

                break;
            case "/list":
                handleCurrencyList(chatId);
                break;
            case "/curse" :
                if (argument.isEmpty()) {
                    sendMessage(chatId, "Какой курс вас интересует?");
                    handleCurrencyList(chatId);
                } else {
                    handleCurrencyRequest(argument.toUpperCase(), chatId);
                }
                break;

            default:
                sendMessage(chatId, "Неизвестная команда. Используете '/help'");
        }
    }

    private void handleCurrencyList(long chatId) {
        try {
            sendMessage(chatId, "Загружаю доступные валюты...");

            String list = currencyService.getFormattedCurrencyList();

            sendMessage(chatId, list, buildCurrencyKeyboard());
        } catch (IOException e) {
            sendMessage(chatId, "Не удалось загрузить список валют.\n" +
                    "Проверьте соединение...");
            e.printStackTrace();
        }
    }

    private void handleCurrencyRequest(String currencyCode, long chatId) {
        try {

            sendMessage(chatId, "🔍 Ищу курс " + currencyCode + "...");

            CurrencyModel currency = currencyService.getCurrency(currencyCode);

            String response = String.format(
                    "Дата: %s \n" +
                    "💰 %s (%s)\n" +
                            "📊 Курс ЦБ РФ: %.2f RUB\n" +
                            "🔢 Номинал: %d единица\n" +
                    "🔄 Изменение: %s (%s) %s"
                    ,
                    currency.getPrettyDate(),
                    currency.getCharCode(),
                    currency.getName(),
                    currency.getValue(),
                    currency.getNominal(),
                    currency.getFormattedDiff(),
                    currency.getFormattedPercentChange(),
                    currency.getChangeSymbol()
            );

            sendMessage(chatId, response);

        } catch (Exception e) {
            sendMessage(chatId,
                    "❌ Не удалось получить курс для " + currencyCode + "\n" +
                            "Проверьте правильность кода валюты"
            );
        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Привет, " + name + "! 👋\n" +
                "Я помогу узнать курс валют ЦБ РФ.\n\n" +
                "📌 Просто напишите код валюты:\n" +
                "   • USD - доллар США\n" +
                "   • EUR - евро\n" +
                "   • CNY - китайский юань\n\n" +
                "Или используйте команды:\n" +
                "   /help - справка\n" +
                "   /list - все валюты";

        sendMessage(chatId, answer, buildCurrencyKeyboard());
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
            System.out.println("✅ Отправлено в чат " + chatId + ": " +
                    textToSend.substring(0, Math.min(50, textToSend.length())) + "...");
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка отправки в чат " + chatId + ": " + e.getMessage());

            try {
                sendMessage.setParseMode(null);
                execute(sendMessage);
            } catch (TelegramApiException e2) {
                System.err.println("❌ Вторая попытка тоже не удалась: " + e2.getMessage());
            }
        }
    }

    private void sendMessage(Long chatId, String textToSend, InlineKeyboardMarkup keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setReplyMarkup(keyboard);

        try {
            execute(sendMessage);
            System.out.println("✅ Отправлено в чат " + chatId + ": " +
                    textToSend.substring(0, Math.min(50, textToSend.length())) + "...");
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка отправки в чат " + chatId + ": " + e.getMessage());

            try {
                sendMessage.setParseMode(null);
                execute(sendMessage);
            } catch (TelegramApiException e2) {
                System.err.println("❌ Вторая попытка тоже не удалась: " + e2.getMessage());
            }
        }
    }

    private InlineKeyboardButton btn(String text, String code) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(code);
        return btn;
    }

    private List<InlineKeyboardButton> addRow(InlineKeyboardButton btn1,
                                              InlineKeyboardButton btn2,
                                              InlineKeyboardButton btn3) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(btn1); row.add(btn2); row.add(btn3);
        return row;
    }

    private InlineKeyboardMarkup buildCurrencyKeyboard() {
        InlineKeyboardButton usdBtn = btn(usdText, "USD");
        InlineKeyboardButton eurBtn = btn(eurText, "EUR");
        InlineKeyboardButton cnyBtn = btn(cnyText, "CNY");

        InlineKeyboardButton gbpBtn = btn(gbpText, "GBP");
        InlineKeyboardButton jpyBtn = btn(jpyText, "JPY");
        InlineKeyboardButton chfBtn = btn(chfText, "CHF");

        InlineKeyboardButton tryBtn = btn(tryText, "TRY");
        InlineKeyboardButton aedBtn = btn(aedText, "AED");
        InlineKeyboardButton kznBtn = btn(kznText, "KZT");

        InlineKeyboardButton bynBtn = btn(bynText, "BYN");
        InlineKeyboardButton cadBtn = btn(cadText, "CAD");
        InlineKeyboardButton hkdBtn = btn(hkdText, "HKD");


        List<InlineKeyboardButton> row1 = addRow(usdBtn, eurBtn, cnyBtn);
        List<InlineKeyboardButton> row2 = addRow(gbpBtn, jpyBtn, chfBtn);
        List<InlineKeyboardButton> row3 = addRow(tryBtn, aedBtn, kznBtn);
        List<InlineKeyboardButton> row4 = addRow(bynBtn, cadBtn, hkdBtn);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row1); rows.add(row2); rows.add(row3); rows.add(row4);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        return markup;
    }
}