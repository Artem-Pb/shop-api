package com.polybezev.currencybot.bot;

import com.polybezev.currencybot.config.BotConfig;
import com.polybezev.currencybot.formatter.MessageFormatter;
import com.polybezev.currencybot.handler.AdminCommandHandler;
import com.polybezev.currencybot.handler.CommandHandler;
import com.polybezev.currencybot.handler.PaymentHandler;
import com.polybezev.currencybot.model.ConversationState;
import com.polybezev.currencybot.model.Tier;
import com.polybezev.currencybot.model.UserConversationData;
import com.polybezev.currencybot.model.UserMode;
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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
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
    private final MessageFormatter messageFormatter;

    @Override
    public String getBotUsername() { return botConfig.getBotName(); }

    @Override
    public String getBotToken() { return botConfig.getToken(); }

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

        if (update.hasMessage() && update.getMessage().hasSuccessfulPayment()) {
            long chatId = update.getMessage().getChatId();
            send(paymentHandler.handleSuccessfulPayment(chatId, update.getMessage().getSuccessfulPayment()));
            return;
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text     = update.getMessage().getText();
        long chatId     = update.getMessage().getChatId();
        String username = UserInfoExtractor.getUserName(update);
        String firstName = UserInfoExtractor.getFirstName(update);

        SubscriptionService.UserRegistration reg =
                subscriptionService.getOrCreateUser(chatId, username, firstName);

        // Забаненные — игнорируем
        if (reg.user().isBanned()) return;

        // Новый пользователь — просто логируем; презентацию покажет /start ниже
        if (reg.isNew()) {
            log.info("New user registered: {}", chatId);
        }

        log.info("User {} sent: {}", chatId, text);

        boolean isAdmin = adminCommandHandler.isAdmin(chatId);

        // Slash-команды администратора (legacy + приоритет)
        if (isAdmin && adminCommandHandler.isAdminCommand(text)) {
            send(adminCommandHandler.handle(text, chatId));
            return;
        }

        UserConversationData data = userStateService.getOrCreate(chatId);
        ConversationState stateBefore = data.getState();
        UserMode modeBefore = data.getMode();

        SendMessage response;

        // Admin panel mode — FSM ввод
        if (modeBefore == UserMode.ADMIN && stateBefore != ConversationState.IDLE) {
            response = adminCommandHandler.handleAdminFsmInput(text, chatId, data);
        }
        // Admin panel mode — кнопки
        else if (modeBefore == UserMode.ADMIN) {
            response = adminCommandHandler.handleAdminText(text, chatId, data, isAdmin);
        }
        // Кнопка "🔧 Админ"
        else if (isAdmin && text.equals("🔧 Админ")) {
            response = adminCommandHandler.enterAdminPanel(chatId, data);
        }
        // Конвертер FSM
        else if (stateBefore != ConversationState.IDLE
                && stateBefore != ConversationState.ADMIN_AWAIT_GRANT_ID
                && stateBefore != ConversationState.ADMIN_AWAIT_GRANT_TIER) {
            response = commandHandler.handleFsmInput(text, chatId, data);
        }
        // Slash-команды
        else if (text.startsWith("/")) {
            response = commandHandler.handleCommand(text, chatId, firstName, data, isAdmin);
        }
        // Текст / Reply-кнопки
        else {
            response = commandHandler.handleText(text, chatId, data, isAdmin);
        }

        // Отправка с поддержкой edit-in-place для FSM конвертера
        sendWithFsmEdit(chatId, data, stateBefore, response);
    }

    private void handleCallback(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        log.info("User {} callback: {}", chatId, data);

        // Всегда убираем "часики" на кнопке
        try {
            AnswerCallbackQuery ack = new AnswerCallbackQuery();
            ack.setCallbackQueryId(update.getCallbackQuery().getId());
            execute(ack);
        } catch (TelegramApiException ignored) {}

        UserConversationData fsm = userStateService.getOrCreate(chatId);
        boolean isAdmin = adminCommandHandler.isAdmin(chatId);

        // Admin callbacks
        if (data.startsWith("ADMIN_")) {
            SendMessage resp = adminCommandHandler.handleAdminCallback(data, chatId, fsm);
            if (resp != null) send(resp);
            return;
        }

        SendMessage response;

        if (fsm.getState() == ConversationState.AWAIT_FROM
                || fsm.getState() == ConversationState.AWAIT_TO) {
            // Выбор валюты через кнопку в FSM — редактируем сообщение
            ConversationState stateBefore = fsm.getState();
            response = commandHandler.handleFsmInput(data, chatId, fsm);
            sendWithFsmEdit(chatId, fsm, stateBefore, response);
            return;
        }

        if (data.startsWith("SIGNAL_")) {
            response = commandHandler.handleSignalForCoin(chatId, data.substring(7));
        } else if (data.equals("BTC")) {
            response = commandHandler.handleBtc(chatId);
        } else if (data.startsWith("BUY_")) {
            Tier tier = Tier.valueOf(data.substring(4));
            try { execute(paymentHandler.sendInvoice(chatId, tier)); }
            catch (TelegramApiException e) {
                log.error("Invoice error {}: {}", chatId, e.getMessage(), e);
            }
            return;
        } else {
            response = commandHandler.handleCurrencyRequest(data, chatId);
        }

        send(response);
    }

    // ==================== FSM EDIT-IN-PLACE ====================

    /**
     * Отправляет или редактирует сообщение в зависимости от контекста FSM.
     * Если пользователь был в FSM (stateBefore != IDLE) и у него есть lastBotMessageId —
     * редактируем то же сообщение. Иначе отправляем новое и запоминаем ID.
     */
    private void sendWithFsmEdit(long chatId, UserConversationData data,
                                  ConversationState stateBefore, SendMessage response) {
        boolean wasInFsm = isConverterFsm(stateBefore);
        Integer prevMsgId = data.getLastBotMessageId();

        if (wasInFsm && prevMsgId != null) {
            boolean edited = tryEdit(chatId, prevMsgId, response);
            if (!edited) {
                Message sent = sendTracked(response);
                if (sent != null) data.setLastBotMessageId(sent.getMessageId());
            }
            UserConversationData fresh = userStateService.getOrCreate(chatId);
            if (!isConverterFsm(fresh.getState())) {
                fresh.setLastBotMessageId(null);
            }
        } else {
            Message sent = sendTracked(response);
            if (sent != null && isConverterFsm(data.getState())) {
                data.setLastBotMessageId(sent.getMessageId());
            }
        }
    }

    private boolean isConverterFsm(ConversationState state) {
        return state == ConversationState.AWAIT_AMOUNT
            || state == ConversationState.AWAIT_FROM
            || state == ConversationState.AWAIT_TO;
    }

    private boolean tryEdit(long chatId, int messageId, SendMessage source) {
        try {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(String.valueOf(chatId));
            edit.setMessageId(messageId);
            edit.setText(source.getText());
            if (source.getReplyMarkup() instanceof InlineKeyboardMarkup kb) {
                edit.setReplyMarkup(kb);
            }
            execute(edit);
            return true;
        } catch (TelegramApiException e) {
            log.warn("Edit failed for msg {} of {}: {}", messageId, chatId, e.getMessage());
            return false;
        }
    }

    // ==================== MESSAGING ====================

    public void send(SendMessage message) {
        if (message == null) return;
        try { execute(message); }
        catch (TelegramApiException e) {
            log.error("Send error for {}: {}", message.getChatId(), e.getMessage(), e);
        }
    }

    /** Отправляет сообщение и возвращает объект Message с id — для FSM edit-in-place. */
    public Message sendTracked(SendMessage message) {
        if (message == null) return null;
        try { return execute(message); }
        catch (TelegramApiException e) {
            log.error("Send error for {}: {}", message.getChatId(), e.getMessage(), e);
            return null;
        }
    }

    // ==================== HELPERS ====================

    private SendMessage buildWelcomeMessage(long chatId, String firstName, boolean isAdmin) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText(messageFormatter.buildWelcomeNewText(firstName));
        msg.setReplyMarkup(messageFormatter.buildMainKeyboard(isAdmin));
        return msg;
    }
}
