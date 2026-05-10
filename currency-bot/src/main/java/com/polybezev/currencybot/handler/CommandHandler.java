package com.polybezev.currencybot.handler;

import com.polybezev.currencybot.formatter.BotMessages;
import com.polybezev.currencybot.formatter.MessageFormatter;
import com.polybezev.currencybot.model.ConversationState;
import com.polybezev.currencybot.model.CryptoPriceModel;
import com.polybezev.currencybot.model.CurrencyModel;
import com.polybezev.currencybot.model.UserConversationData;
import com.polybezev.currencybot.service.CryptoService;
import com.polybezev.currencybot.service.CurrencyService;
import com.polybezev.currencybot.service.UserStateService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.IOException;

@Component
@AllArgsConstructor
@Slf4j
public class CommandHandler {

    private final CurrencyService currencyService;
    private final MessageFormatter formatter;
    private final CryptoService cryptoService;
    private final UserStateService userStateService;

    // ==================== ENTRY POINTS ====================

    public SendMessage handleCommand(String text, long chatId, String userName) {
        String[] parts = text.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].trim() : "";

        return switch (cmd) {
            case "/start" -> msg(chatId, formatter.buildStartText(userName), formatter.buildMainKeyboard());
            case "/help" -> msg(chatId, formatter.buildHelpText());
            case "/list" -> handleList(chatId);
            case "/curse" -> arg.isEmpty() ? handleList(chatId) : handleCurrencyRequest(arg.toUpperCase(), chatId);
            case "/convert" -> arg.isEmpty() ? startConvertFsm(chatId) : handleConvert(arg, chatId);
            case "/btc" -> handleBtc(chatId);
            default -> msg(chatId, BotMessages.UNKNOWN_COMMAND);
        };
    }

    public SendMessage handleText(String text, long chatId) {
        if (text.equals("📊 Курсы")) return handleList(chatId);
        if (text.equals("💱 Конвертер")) return startConvertFsm(chatId);
        if (text.equals("₿ BTC")) return handleBtc(chatId);

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
            log.warn("Currency not found: {} for user {}", code, chatId);
            return msg(chatId, formatter.buildCurrencyNotFoundText(code));
        }
    }

    private SendMessage handleList(long chatId) {
        try {
            String list = currencyService.getFormattedCurrencyList();
            return msg(chatId, list, formatter.buildConvertKeyboard());
        } catch (IOException e) {
            log.error("API unavailable for user {}: {}", chatId, e.getMessage(), e);
            return msg(chatId, BotMessages.LIST_ERROR);
        }
    }

    public SendMessage handleConvert(String arg, Long chatId) {
        String[] parts = arg.split("\\s+");

        if (parts.length != 3) {
            return msg(chatId, BotMessages.CONVERT_FORMAT_ERROR);
        }

        try {
            double amount = Double.parseDouble(parts[0]);
            String from = parts[1].toUpperCase();
            String to = parts[2].toUpperCase();

            double result = currencyService.convertCurrency(amount, from, to);
            return msg(chatId, formatter.buildConvertResult(amount, from, result, to));

        } catch (NumberFormatException e) {
            log.warn("Invalid amount input from user {}: {}", chatId, e.getMessage());
            return msg(chatId, BotMessages.CONVERT_AMOUNT_ERROR);

        } catch (Exception e) {
            log.error("API unavailable for user {}: {}", chatId, e.getMessage(), e);
            return msg(chatId, BotMessages.CONVERT_ERROR);
        }
    }

    public SendMessage handleBtc(long chatId) {
        try {
            CryptoPriceModel model = cryptoService.getCryptoPrice("bitcoin");
            model.setSymbol("BTC");
            return msg(chatId, formatter.buildCryptoCard(model));
        } catch (IOException e) {
            log.error("API unavailable for user {}: {}", chatId, e.getMessage(), e);
            return msg(chatId, BotMessages.BTC_ERROR);
        }
    }

    public SendMessage handleFsmInput(String text, long chatId, UserConversationData data) {
        return switch (data.getState()) {
            case AWAIT_AMOUNT -> {
                try {
                    double amount = Double.parseDouble(text);
                    data.setAmount(amount);
                    data.setState(ConversationState.AWAIT_FROM);
                    yield msg(chatId, BotMessages.CONVERT_AWAIT_FROM, formatter.buildConvertKeyboard());
                } catch (NumberFormatException e) {
                    yield msg(chatId, BotMessages.CONVERT_AMOUNT_INVALID);
                }
            }
            case AWAIT_FROM   -> {
                data.setFromCurrency(text);
                data.setState(ConversationState.AWAIT_TO);
                yield msg(chatId, BotMessages.CONVERT_AWAIT_TO, formatter.buildConvertKeyboard());
            }
            case AWAIT_TO     -> {
                double amount = data.getAmount();
                String from = data.getFromCurrency();
                try {
                    double result = currencyService.convertCurrency(amount, from, text);
                    userStateService.reset(chatId);
                    yield msg(chatId, formatter.buildConvertResult(amount, from, result, text));
                } catch (IOException e) {
                    log.error("API unavailable for user {}: {}", chatId, e.getMessage(), e);
                    yield msg(chatId, BotMessages.CONVERT_ERROR);
                }
            }
            default           -> msg(chatId, BotMessages.CONVERT_FSM_ERROR);
        };
    }

    // ==================== BUILDERS ====================

    private SendMessage msg(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        return message;
    }

    private SendMessage msg(long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = msg(chatId, text);
        message.setReplyMarkup(keyboard);
        return message;
    }

    private SendMessage startConvertFsm(long chatId) {
        UserConversationData data = userStateService.getOrCreate(chatId);
        data.setState(ConversationState.AWAIT_AMOUNT);
        return msg(chatId, BotMessages.CONVERT_AWAIT_AMOUNT);
    }
}
