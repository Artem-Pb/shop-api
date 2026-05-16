package com.polybezev.currencybot.handler;

import com.polybezev.currencybot.entity.UserSubscription;
import com.polybezev.currencybot.formatter.BotMessages;
import com.polybezev.currencybot.formatter.MessageFormatter;
import com.polybezev.currencybot.model.*;
import com.polybezev.currencybot.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Component
@AllArgsConstructor
@Slf4j
public class CommandHandler {

    private final CurrencyService currencyService;
    private final MessageFormatter formatter;
    private final CryptoService cryptoService;
    private final UserStateService userStateService;
    private final SubscriptionService subscriptionService;
    private final TaSignalService taSignalService;
    private final AiAnalysisService aiAnalysisService;
    private final NewsService newsService;
    private final MarketDataService marketDataService;

    // ==================== ENTRY POINTS ====================

    /**
     * @param isAdmin true если chatId совпадает с adminChatId — меняет клавиатуру /start
     */
    public SendMessage handleCommand(String text, long chatId, String firstName,
                                     UserConversationData data, boolean isAdmin) {
        String[] parts = text.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String arg = parts.length > 1 ? parts[1].trim() : "";

        return switch (cmd) {
            case "/start" -> {
                data.setMode(UserMode.MAIN);
                yield msg(chatId, formatter.buildStartText(firstName), formatter.buildMainKeyboard(isAdmin));
            }
            case "/help"    -> msg(chatId, formatter.buildHelpText());
            case "/list"    -> handleList(chatId);
            case "/curse"   -> arg.isEmpty() ? showCurseKeyboard(chatId) : handleCurrencyRequest(arg.toUpperCase(), chatId);
            case "/convert" -> arg.isEmpty() ? startConvertFsm(chatId, data) : handleConvert(arg, chatId);
            case "/btc"     -> handleBtc(chatId);
            case "/tier"    -> handleTier(chatId, isAdmin);
            case "/signal"  -> arg.isEmpty() ? handleSignal(chatId) : handleSignalForCoin(chatId, arg.toUpperCase());
            case "/lk"      -> enterLk(chatId, data);
            default         -> msg(chatId, BotMessages.UNKNOWN_COMMAND);
        };
    }

    public SendMessage handleText(String text, long chatId, UserConversationData data, boolean isAdmin) {
        // ЛК режим
        if (data.getMode() == UserMode.LK) {
            return handleLkText(text, chatId, data, isAdmin);
        }

        // Главное меню — Reply кнопки
        if (text.equals("📊 Курсы"))    return handleList(chatId);
        if (text.equals("Конвертер"))   return startConvertFsm(chatId, data);
        if (text.equals("₿ BTC"))       return handleBtc(chatId);
        if (text.equals("📈 Сигналы"))  return handleSignal(chatId);
        if (text.equals("💎 Подписка")) return handleTier(chatId, isAdmin);
        if (text.equals("👤 ЛК"))       return enterLk(chatId, data);
        if (text.equals("❓ Помощь"))   return msg(chatId, formatter.buildHelpText());

        // Распознавание свободного ввода
        String upper = text.toUpperCase().trim();
        if (upper.matches("[A-Z]{3}"))         return handleCurrencyRequest(upper, chatId);
        if (upper.contains("ДОЛЛАР"))          return handleCurrencyRequest("USD", chatId);
        if (upper.contains("ЕВРО"))            return handleCurrencyRequest("EUR", chatId);
        if (upper.contains("ЮАНЬ"))            return handleCurrencyRequest("CNY", chatId);

        return msg(chatId, formatter.buildUnknownInputText());
    }

    // ==================== ЛК — Личный кабинет ====================

    public SendMessage enterLk(long chatId, UserConversationData data) {
        data.setMode(UserMode.LK);
        Tier tier = subscriptionService.getActiveTier(chatId);
        return msg(chatId, BotMessages.LK_HEADER, formatter.buildLkKeyboard(tier));
    }

    private SendMessage handleLkText(String text, long chatId, UserConversationData data, boolean isAdmin) {
        switch (text) {
            case "💰 Баланс"    -> { return handleLkBalance(chatId); }
            case "⭐ Подписка"  -> { return handleTier(chatId, isAdmin); }
            case "📰 Новости"   -> { return handleNews(chatId); }
            case "📊 AI-сводка" -> { return handleAiDigest(chatId); }
            case "🤖 Торговый бот" -> { return msg(chatId, "🤖 Торговый бот — в разработке. TIER 3 скоро."); }
            case "❓ Помощь"    -> { return msg(chatId, BotMessages.LK_HELP); }
            case "◀️ Назад" -> {
                data.setMode(UserMode.MAIN);
                return msg(chatId, BotMessages.START_TEXT.replace("{name}", ""), formatter.buildMainKeyboard(isAdmin));
            }
            default -> { return msg(chatId, formatter.buildUnknownInputText()); }
        }
    }

    private SendMessage handleLkBalance(long chatId) {
        Tier tier = subscriptionService.getActiveTier(chatId);
        String expires = subscriptionService.getActiveSubscription(chatId)
                .map(sub -> sub.getExpiresAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .orElse("—");
        int paid = subscriptionService.getActiveSubscription(chatId)
                .map(sub -> sub.getAmountPaid().intValue())
                .orElse(0);
        return msg(chatId, formatter.buildLkBalance(tier, expires, paid));
    }

    // ==================== TIER 1: Новости по запросу ====================

    public SendMessage handleNews(long chatId) {
        if (!subscriptionService.hasAccess(chatId, Tier.TIER_1)) {
            return msg(chatId, BotMessages.NEWS_TIER_REQUIRED);
        }
        try {
            String news = newsService.getTopNewsFresh();
            return msg(chatId, BotMessages.NEWS_HEADER + news);
        } catch (IOException e) {
            log.error("News fetch error for user {}: {}", chatId, e.getMessage());
            return msg(chatId, BotMessages.NEWS_ERROR);
        }
    }

    // ==================== TIER 2: AI-сводка по запросу ====================

    private SendMessage handleAiDigest(long chatId) {
        if (!subscriptionService.hasAccess(chatId, Tier.TIER_2)) {
            return msg(chatId, BotMessages.DIGEST_TIER_REQUIRED);
        }
        try {
            String marketData = marketDataService.getMarketSnapshot();
            String news = newsService.getTopNews();
            String digest = aiAnalysisService.generateMorningDigest(marketData, news);
            return msg(chatId, digest);
        } catch (Exception e) {
            log.error("On-demand digest error for user {}: {}", chatId, e.getMessage(), e);
            return msg(chatId, "⚠️ Не удалось сформировать сводку. Попробуй позже.");
        }
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
            return msg(chatId, formatter.buildCurrencyList(currencyService.getCurrencyList()),
                    formatter.buildRatesKeyboard());
        } catch (IOException e) {
            log.error("API unavailable for user {}: {}", chatId, e.getMessage(), e);
            return msg(chatId, BotMessages.LIST_ERROR);
        }
    }

    public SendMessage handleConvert(String arg, Long chatId) {
        String[] parts = arg.split("\\s+");
        if (parts.length != 3) return msg(chatId, BotMessages.CONVERT_FORMAT_ERROR);
        try {
            double amount = Double.parseDouble(parts[0]);
            String from = parts[1].toUpperCase();
            String to = parts[2].toUpperCase();
            double result = currencyService.convertCurrency(amount, from, to);
            return msg(chatId, formatter.buildConvertResult(amount, from, result, to));
        } catch (NumberFormatException e) {
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
            case AWAIT_FROM -> {
                data.setFromCurrency(text);
                data.setState(ConversationState.AWAIT_TO);
                yield msg(chatId, BotMessages.CONVERT_AWAIT_TO, formatter.buildConvertKeyboard());
            }
            case AWAIT_TO -> {
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
            default -> msg(chatId, BotMessages.CONVERT_FSM_ERROR);
        };
    }

    public SendMessage handleSignal(long chatId) {
        if (!subscriptionService.hasAccess(chatId, Tier.TIER_2)) {
            return msg(chatId, BotMessages.SIGNAL_TIER_REQUIRED);
        }
        return msg(chatId, BotMessages.SIGNAL_PROMPT, formatter.buildSignalKeyboard());
    }

    public SendMessage handleSignalForCoin(long chatId, String symbol) {
        if (!subscriptionService.hasAccess(chatId, Tier.TIER_2)) {
            return msg(chatId, BotMessages.SIGNAL_TIER_REQUIRED);
        }
        try {
            TaSignalService.SignalResult result = taSignalService.analyzeBySymbol(symbol);
            String aiExplanation = aiAnalysisService.generateSignalExplanation(result);
            SendMessage m = msg(chatId, formatter.buildSignalCard(result, aiExplanation));
            m.setParseMode("Markdown");
            return m;
        } catch (IllegalArgumentException e) {
            return msg(chatId, BotMessages.SIGNAL_UNKNOWN_COIN);
        } catch (Exception e) {
            log.error("TA signal error for user {}: {}", chatId, e.getMessage(), e);
            return msg(chatId, "⚠️ Не удалось получить данные. Попробуй позже.");
        }
    }

    private SendMessage handleTier(long chatId, boolean isAdmin) {
        Tier tier = subscriptionService.getActiveTier(chatId);
        return msg(chatId, formatter.buildTierCard(tier), formatter.buildTierKeyboard());
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

    private SendMessage startConvertFsm(long chatId, UserConversationData data) {
        data.setState(ConversationState.AWAIT_AMOUNT);
        return msg(chatId, BotMessages.CONVERT_AWAIT_AMOUNT);
    }

    private SendMessage showCurseKeyboard(long chatId) {
        return msg(chatId, BotMessages.CURSE_KEYBOARD_PROMPT, formatter.buildRatesKeyboard());
    }
}
