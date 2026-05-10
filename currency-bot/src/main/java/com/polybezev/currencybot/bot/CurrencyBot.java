package com.polybezev.currencybot.bot;

import com.polybezev.currencybot.config.BotConfig;
import com.polybezev.currencybot.handler.CommandHandler;
import com.polybezev.currencybot.util.UserInfoExtractor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@AllArgsConstructor
public class CurrencyBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final CommandHandler commandHandler;

    @Override
    public String getBotUsername() { return botConfig.getBotName(); }

    @Override
    public String getBotToken() { return botConfig.getToken(); }

    // ==================== ROUTING ====================

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text     = update.getMessage().getText();
        long   chatId   = update.getMessage().getChatId();
        String userName = UserInfoExtractor.getFirstName(update);

        SendMessage response = text.startsWith("/")
                ? commandHandler.handleCommand(text, chatId, userName)
                : commandHandler.handleText(text, chatId);

        send(response);
    }

    private void handleCallback(Update update) {
        long   chatId = update.getCallbackQuery().getMessage().getChatId();
        String data   = update.getCallbackQuery().getData();

        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            execute(answer);
        } catch (TelegramApiException ignored) {}

        send(commandHandler.handleCurrencyRequest(data, chatId));
    }

    // ==================== MESSAGING ====================

    public void send(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка отправки: " + e.getMessage());
        }
    }
}
