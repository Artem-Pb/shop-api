package com.polybezev.currencybot.bot;

import com.polybezev.currencybot.util.UserInfoExtractor;
import com.polybezev.currencybot.config.BotConfig;
import com.polybezev.currencybot.model.CurrencyModel;
import com.polybezev.currencybot.service.CurrencyService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Component
@AllArgsConstructor

public class CurrencyBot extends TelegramLongPollingBot {
    private final BotConfig botConfig;

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

            String list = CurrencyService.getFormattedCurrencyList();

            sendMessage(chatId, list);
        } catch (IOException e) {
            sendMessage(chatId, "Не удалось загрузить список валют.\n" +
                    "Проверьте соединение...");
            e.printStackTrace();
        }
    }

    private void handleCurrencyRequest(String currencyCode, long chatId) {
        try {

            sendMessage(chatId, "🔍 Ищу курс " + currencyCode + "...");

            CurrencyModel currency = CurrencyService.getCurrency(currencyCode);

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

        sendMessage(chatId, answer);
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
}