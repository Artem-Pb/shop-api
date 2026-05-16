package com.polybezev.currencybot.handler;

import com.polybezev.currencybot.config.BotConfig;
import com.polybezev.currencybot.entity.User;
import com.polybezev.currencybot.formatter.BotMessages;
import com.polybezev.currencybot.formatter.MessageFormatter;
import com.polybezev.currencybot.model.ConversationState;
import com.polybezev.currencybot.model.Tier;
import com.polybezev.currencybot.model.UserConversationData;
import com.polybezev.currencybot.model.UserMode;
import com.polybezev.currencybot.repository.UserRepository;
import com.polybezev.currencybot.scheduler.MorningDigestScheduler;
import com.polybezev.currencybot.service.SubscriptionService;
import com.polybezev.currencybot.service.UserStateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AdminCommandHandler {

    private final BotConfig botConfig;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final UserStateService userStateService;
    private final MessageFormatter formatter;

    @Lazy
    @Autowired
    private MorningDigestScheduler digestScheduler;

    private static final int PAGE_SIZE = 5;

    public AdminCommandHandler(BotConfig botConfig,
                               SubscriptionService subscriptionService,
                               UserRepository userRepository,
                               UserStateService userStateService,
                               MessageFormatter formatter) {
        this.botConfig = botConfig;
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
        this.userStateService = userStateService;
        this.formatter = formatter;
    }

    public boolean isAdmin(long chatId) {
        return chatId == botConfig.getAdminChatId();
    }

    public boolean isAdminCommand(String text) {
        return text.equals("/digest") || text.startsWith("/grant") || text.startsWith("/ban");
    }

    // ==================== SLASH КОМАНДЫ ====================

    public SendMessage handle(String text, long chatId) {
        if (text.equals("/digest")) {
            digestScheduler.sendMorningDigest();
            return msg(chatId, "Дайджест запущен");
        }
        if (text.startsWith("/grant")) return handleGrant(text, chatId);
        if (text.startsWith("/ban"))   return handleBanCommand(text, chatId);
        return msg(chatId, "Неизвестная команда");
    }

    private SendMessage handleGrant(String text, long chatId) {
        String[] parts = text.split(" ");
        if (parts.length != 3) return msg(chatId, "Формат: /grant <chatId> <TIER_1|TIER_2|TIER_3>");
        try {
            long targetId = Long.parseLong(parts[1]);
            Tier tier = Tier.valueOf(parts[2].toUpperCase());
            subscriptionService.activateSubscription(targetId, tier, 0);
            log.info("Admin {} granted {} to {}", chatId, tier, targetId);
            return msg(chatId, "✅ " + tier.label + " выдан пользователю " + targetId);
        } catch (NumberFormatException e) {
            return msg(chatId, "Формат: /grant <chatId> <TIER_1|TIER_2|TIER_3>");
        } catch (IllegalArgumentException e) {
            return msg(chatId, "Неизвестный тир. Доступны: TIER_1, TIER_2, TIER_3");
        }
    }

    private SendMessage handleBanCommand(String text, long chatId) {
        String[] parts = text.split(" ");
        if (parts.length != 2) return msg(chatId, "Формат: /ban <chatId>");
        try {
            long targetId = Long.parseLong(parts[1]);
            subscriptionService.banUser(targetId);
            return msg(chatId, BotMessages.ADMIN_BAN_SUCCESS.replace("{chatId}", String.valueOf(targetId)));
        } catch (NumberFormatException e) {
            return msg(chatId, "chatId должен быть числом");
        }
    }

    // ==================== КНОПОЧНЫЙ РЕЖИМ АДМИНКИ ====================

    public SendMessage enterAdminPanel(long chatId, UserConversationData data) {
        data.setMode(UserMode.ADMIN);
        data.setState(ConversationState.IDLE);
        return msg(chatId, BotMessages.ADMIN_HEADER, formatter.buildAdminKeyboard());
    }

    /**
     * Обрабатывает нажатия Reply-кнопок в режиме ADMIN.
     */
    public SendMessage handleAdminText(String text, long chatId, UserConversationData data, boolean isAdmin) {
        switch (text) {
            case "👥 Пользователи" -> { return showUserList(chatId, 0); }
            case "🔑 Выдать доступ" -> {
                data.setState(ConversationState.ADMIN_AWAIT_GRANT_ID);
                return msg(chatId, BotMessages.ADMIN_GRANT_ASK_ID);
            }
            case "🚫 Баны" -> { return showBannedList(chatId); }
            case "◀️ Назад" -> {
                data.setMode(UserMode.MAIN);
                data.setState(ConversationState.IDLE);
                return msg(chatId, BotMessages.START_TEXT.replace("{name}", ""),
                        formatter.buildMainKeyboard(isAdmin));
            }
            default -> { return msg(chatId, "Выбери действие из меню"); }
        }
    }

    /**
     * Обрабатывает FSM ввод для Admin Grant flow (ADMIN_AWAIT_GRANT_ID → ADMIN_AWAIT_GRANT_TIER).
     */
    public SendMessage handleAdminFsmInput(String text, long chatId, UserConversationData data) {
        return switch (data.getState()) {
            case ADMIN_AWAIT_GRANT_ID -> {
                try {
                    Long.parseLong(text.trim());
                    data.setAdminGrantTargetId(text.trim());
                    data.setState(ConversationState.ADMIN_AWAIT_GRANT_TIER);
                    String prompt = BotMessages.ADMIN_GRANT_ASK_TIER
                            .replace("{targetId}", text.trim());
                    yield msg(chatId, prompt, formatter.buildAdminGrantTierKeyboard());
                } catch (NumberFormatException e) {
                    yield msg(chatId, BotMessages.ADMIN_GRANT_ID_ERROR);
                }
            }
            default -> msg(chatId, "Неожиданное состояние. Начни заново.");
        };
    }

    /**
     * Обрабатывает callback кнопок в админ-режиме (ADMIN_GRANT_TIER_*, ADMIN_BAN_*, ADMIN_UNBAN_*).
     */
    public SendMessage handleAdminCallback(String data, long chatId, UserConversationData fsm) {
        if (data.startsWith("ADMIN_GRANT_TIER_")) {
            String tierName = "TIER_" + data.substring("ADMIN_GRANT_TIER_".length());
            String targetId = fsm.getAdminGrantTargetId();
            if (targetId == null) return msg(chatId, "Ошибка: потерян targetId. Начни заново.");
            try {
                Tier tier = Tier.valueOf(tierName);
                subscriptionService.activateSubscription(Long.parseLong(targetId), tier, 0);
                fsm.setState(ConversationState.IDLE);
                fsm.setAdminGrantTargetId(null);
                log.info("Admin {} granted {} to {}", chatId, tier, targetId);
                return msg(chatId, BotMessages.ADMIN_GRANT_SUCCESS
                        .replace("{targetId}", targetId)
                        .replace("{tier}", tier.label));
            } catch (Exception e) {
                return msg(chatId, "Ошибка выдачи: " + e.getMessage());
            }
        }
        if (data.startsWith("ADMIN_BAN_")) {
            long targetId = Long.parseLong(data.substring("ADMIN_BAN_".length()));
            subscriptionService.banUser(targetId);
            return msg(chatId, BotMessages.ADMIN_BAN_SUCCESS.replace("{chatId}", String.valueOf(targetId)));
        }
        if (data.startsWith("ADMIN_UNBAN_")) {
            long targetId = Long.parseLong(data.substring("ADMIN_UNBAN_".length()));
            subscriptionService.unbanUser(targetId);
            return msg(chatId, BotMessages.ADMIN_UNBAN_SUCCESS.replace("{chatId}", String.valueOf(targetId)));
        }
        if (data.startsWith("ADMIN_START_GRANT_")) {
            long targetId = Long.parseLong(data.substring("ADMIN_START_GRANT_".length()));
            fsm.setAdminGrantTargetId(String.valueOf(targetId));
            fsm.setState(ConversationState.ADMIN_AWAIT_GRANT_TIER);
            String prompt = BotMessages.ADMIN_GRANT_ASK_TIER.replace("{targetId}", String.valueOf(targetId));
            return msg(chatId, prompt, formatter.buildAdminGrantTierKeyboard());
        }
        return null;
    }

    // ==================== СПИСКИ ====================

    public SendMessage showUserList(long chatId, int page) {
        Page<User> userPage = userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, PAGE_SIZE));
        List<String> lines = new ArrayList<>();
        for (User u : userPage.getContent()) {
            Tier tier = subscriptionService.getActiveTier(u.getChatId());
            String banned = u.isBanned() ? " 🚫" : "";
            String name = u.getFirstName() != null ? u.getFirstName() : "—";
            String username = u.getUsername() != null ? " (@" + u.getUsername() + ")" : "";
            lines.add(String.format("%s%s%s\n  ID: %d · %s",
                    name, username, banned, u.getChatId(), tier.label));
        }
        return msg(chatId,
                formatter.buildAdminUserList(lines, page + 1, userPage.getTotalPages()));
    }

    private SendMessage showBannedList(long chatId) {
        long count = userRepository.countByBanned(true);
        if (count == 0) return msg(chatId, "🚫 Забаненных пользователей нет.");
        Page<User> banned = userRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 20));
        List<String> lines = new ArrayList<>();
        banned.getContent().stream()
                .filter(User::isBanned)
                .forEach(u -> lines.add(u.getChatId() + " — " + u.getFirstName()));
        return msg(chatId, "🚫 Заблокированные (" + count + "):\n\n" + String.join("\n", lines));
    }

    // ==================== HELPERS ====================

    private SendMessage msg(long chatId, String text) {
        SendMessage m = new SendMessage();
        m.setChatId(String.valueOf(chatId));
        m.setText(text);
        return m;
    }

    private SendMessage msg(long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage m = msg(chatId, text);
        m.setReplyMarkup(keyboard);
        return m;
    }
}
