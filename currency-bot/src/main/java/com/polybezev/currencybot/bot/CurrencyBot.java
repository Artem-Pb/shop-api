package com.polybezev.currencybot.bot;

import com.polybezev.currencybot.config.BotConfig;
import com.polybezev.currencybot.handler.AdminCommandHandler;
import com.polybezev.currencybot.handler.CommandHandler;
import com.polybezev.currencybot.handler.PaymentHandler;
import com.polybezev.currencybot.model.ConversationState;
import com.polybezev.currencybot.model.Tier;
import com.polybezev.currencybot.model.UserConversationData;
import com.polybezev.currencybot.service.SubscriptionService;
import com.polybezev.currencybot.service.UserStateService;
import com.polybezev.currencybot.util.UserInfoExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CurrencyBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final CommandHandler commandHandler;
    private final AdminCommandHandler adminCommandHandler;
    private final UserStateService userStateService;
    private final SubscriptionService subscriptionService;
    private final PaymentHandler paymentHandler;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    // ==================== ROUTING ====================

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasPreCheckoutQuery()) {
            AnswerPreCheckoutQuery answer = new AnswerPreCheckoutQuery();
            answer.setPreCheckoutQueryId(update.getPreCheckoutQuery().getId());
            answer.setOk(true);
            try { execute(answer); } catch (TelegramApiException ignored) {}
            return;
        }

        if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        }

        if (update.getMessage().hasSuccessfulPayment()) {
            long chatId = update.getMessage().getChatId();
            send(paymentHandler.handleSuccessfulPayment(chatId, update.getMessage().getSuccessfulPayment()));
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        String username = UserInfoExtractor.getUserName(update);
        String firstName = UserInfoExtractor.getFirstName(update);
        subscriptionService.getOrCreateUser(chatId, username, firstName);
        log.info("User {} sent: {}", chatId, text);

        if (adminCommandHandler.isAdmin(chatId) && adminCommandHandler.isAdminCommand(text)) {
            send(adminCommandHandler.handle(text, chatId));
            return;
        }

        UserConversationData data = userStateService.getOrCreate(chatId);
        SendMessage response;

        if (data.getState() != ConversationState.IDLE) {
            response = commandHandler.handleFsmInput(text, chatId, data);
        } else if (text.startsWith("/")) {
            response = commandHandler.handleCommand(text, chatId, firstName);
        } else {
            response = commandHandler.handleText(text, chatId);
        }

        send(response);
    }

    private void handleCallback(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        log.info("User {} pressed button: {}", chatId, data);

        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(update.getCallbackQuery().getId());
            execute(answer);
        } catch (TelegramApiException ignored) {}

        UserConversationData fsm = userStateService.getOrCreate(chatId);
        if (fsm.getState() == ConversationState.AWAIT_FROM || fsm.getState() == ConversationState.AWAIT_TO) {
            send(commandHandler.handleFsmInput(data, chatId, fsm));
        } else if (data.equals("BTC")) {
            send(commandHandler.handleBtc(chatId));
        } else if (data.startsWith("BUY_")) {
            Tier tier = Tier.valueOf(data.substring(4));
            try {
                execute(paymentHandler.sendInvoice(chatId, tier));
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки инвойса {}: {}", chatId, e.getMessage(), e);
            }
        } else {
            send(commandHandler.handleCurrencyRequest(data, chatId));
        }
    }

    // ==================== MESSAGING ====================

    public void send(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения пользователю {}: {}", message.getChatId(), e.getMessage(), e);
        }
    }
}
