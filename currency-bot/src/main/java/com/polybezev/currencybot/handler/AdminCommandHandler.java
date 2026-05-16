package com.polybezev.currencybot.handler;

import com.polybezev.currencybot.config.BotConfig;
import com.polybezev.currencybot.model.Tier;
import com.polybezev.currencybot.scheduler.MorningDigestScheduler;
import com.polybezev.currencybot.service.SubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
public class AdminCommandHandler {

    private final BotConfig botConfig;
    private final SubscriptionService subscriptionService;

    // @Lazy разрывает цикл: MorningDigestScheduler → CurrencyBot → AdminCommandHandler → MorningDigestScheduler
    @Lazy
    @Autowired
    private MorningDigestScheduler digestScheduler;

    public AdminCommandHandler(BotConfig botConfig, SubscriptionService subscriptionService) {
        this.botConfig = botConfig;
        this.subscriptionService = subscriptionService;
    }

    public boolean isAdminCommand(String text) {
        return text.equals("/digest") || text.startsWith("/grant");
    }

    public boolean isAdmin(long chatId) {
        return chatId == botConfig.getAdminChatId();
    }

    public SendMessage handle(String text, long chatId) {
        if (text.equals("/digest")) {
            digestScheduler.sendMorningDigest();
            return msg(chatId, "Дайджест запущен");
        }
        if (text.startsWith("/grant")) {
            return handleGrant(text, chatId);
        }
        return msg(chatId, "Неизвестная команда");
    }

    private SendMessage handleGrant(String text, long chatId) {
        String[] parts = text.split(" ");
        if (parts.length != 2) return msg(chatId, "Формат: /grant <chatId>");
        try {
            long targetId = Long.parseLong(parts[1]);
            subscriptionService.activateSubscription(targetId, Tier.TIER_1, 0);
            log.info("Admin {} granted TIER_1 to {}", chatId, targetId);
            return msg(chatId, "✅ TIER_1 выдан пользователю " + targetId);
        } catch (NumberFormatException e) {
            return msg(chatId, "Формат: /grant <chatId>");
        }
    }

    private SendMessage msg(long chatId, String text) {
        SendMessage m = new SendMessage();
        m.setChatId(String.valueOf(chatId));
        m.setText(text);
        return m;
    }
}
