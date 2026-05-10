package com.polybezev.currencybot.service;

import com.polybezev.currencybot.model.Tier;
import com.polybezev.currencybot.model.User;
import com.polybezev.currencybot.model.UserSubscription;
import com.polybezev.currencybot.repository.UserRepository;
import com.polybezev.currencybot.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;

    public User getOrCreateUser(Long chatId, String username, String firstName) {
        return userRepository.findByChatId(chatId)
                .orElseGet(() -> {
                    User user = new User();
                    user.setChatId(chatId);
                    user.setUsername(username);
                    user.setFirstName(firstName);
                    user.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                });
    }

    public Tier getActiveTier(Long chatId) {
        return userRepository.findByChatId(chatId)
                .flatMap(userSubscriptionRepository::findByUserAndActiveTrue)
                .filter(sub -> sub.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(UserSubscription::getTier)
                .orElse(Tier.FREE);
    }


    public boolean hasAccess(Long chatId, Tier required) {
        return getActiveTier(chatId).ordinal() >= required.ordinal();
    }
}
