package com.polybezev.currencybot.scheduler;

import com.polybezev.currencybot.bot.CurrencyBot;
import com.polybezev.currencybot.entity.UserSubscription;
import com.polybezev.currencybot.repository.UserSubscriptionRepository;
import com.polybezev.currencybot.service.AiAnalysisService;
import com.polybezev.currencybot.service.MarketDataService;
import com.polybezev.currencybot.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MorningDigestScheduler {

    private final UserSubscriptionRepository subscriptionRepository;
    private final MarketDataService marketDataService;
    private final NewsService newsService;
    private final AiAnalysisService aiAnalysisService;
    private final CurrencyBot currencyBot;

    @Scheduled(cron = "0 0 8 * * *", zone = "Europe/Moscow")
    public void sendMorningDigest() {
        List<UserSubscription> subscribers = subscriptionRepository.findAllActivePaid();
        log.info("Starting morning digest for {} subscribers", subscribers.size());
        if (subscribers.isEmpty()) return;

        try {
            String marketData = marketDataService.getMarketSnapshot();
            String news = newsService.getTopNews();
            String digest = aiAnalysisService.generateMorningDigest(marketData, news);

            for (UserSubscription sub : subscribers) {
                long chatId = sub.getUser().getChatId();
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(digest);
                currencyBot.send(message);
                log.info("Morning digest sent to user {}", chatId);
            }
        } catch (Exception e) {
            log.error("Morning digest failed: {}", e.getMessage(), e);
        }
    }

    public void sendDigestToUser(long chatId) {
        try {
            String marketData = marketDataService.getMarketSnapshot();
            String news = newsService.getTopNews();
            String digest = aiAnalysisService.generateMorningDigest(marketData, news);
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(digest);
            currencyBot.send(message);
            log.info("Test digest sent to {}", chatId);
        } catch (Exception e) {
            log.error("Test digest failed: {}", e.getMessage(), e);
        }
    }
}
