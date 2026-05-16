package com.polybezev.currencybot.service;

import com.polybezev.currencybot.model.PaymentProvider;
import com.polybezev.currencybot.model.Tier;
import com.polybezev.currencybot.entity.User;
import com.polybezev.currencybot.entity.UserSubscription;
import com.polybezev.currencybot.repository.UserRepository;
import com.polybezev.currencybot.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;

    public record UserRegistration(User user, boolean isNew) {}

    /**
     * Upsert пользователя по chatId.
     * Возвращает флаг isNew=true если пользователь создан впервые.
     */
    public UserRegistration getOrCreateUser(Long chatId, String username, String firstName) {
        Optional<User> existing = userRepository.findByChatId(chatId);
        if (existing.isPresent()) return new UserRegistration(existing.get(), false);

        User user = new User();
        user.setChatId(chatId);
        user.setUsername(username);
        user.setFirstName(firstName != null ? firstName : "User");
        user.setCreatedAt(LocalDateTime.now());
        return new UserRegistration(userRepository.save(user), true);
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

    @Transactional
    public void activateSubscription(Long chatId, Tier tier, int amountStars) {
        User user = getOrCreateUser(chatId, null, "User").user();

        userSubscriptionRepository.findByUserAndActiveTrue(user)
                .ifPresent(sub -> {
                    sub.setActive(false);
                    userSubscriptionRepository.save(sub);
                });

        UserSubscription sub = new UserSubscription();
        sub.setUser(user);
        sub.setTier(tier);
        sub.setPaymentProvider(PaymentProvider.STARS);
        sub.setAmountPaid(BigDecimal.valueOf(amountStars));
        sub.setStartedAt(LocalDateTime.now());
        sub.setExpiresAt(LocalDateTime.now().plusDays(30));
        sub.setActive(true);
        userSubscriptionRepository.save(sub);
    }

    @Transactional
    public void banUser(Long chatId) {
        userRepository.findByChatId(chatId).ifPresent(u -> {
            u.setBanned(true);
            userRepository.save(u);
        });
    }

    @Transactional
    public void unbanUser(Long chatId) {
        userRepository.findByChatId(chatId).ifPresent(u -> {
            u.setBanned(false);
            userRepository.save(u);
        });
    }

    public Optional<UserSubscription> getActiveSubscription(Long chatId) {
        return userRepository.findByChatId(chatId)
                .flatMap(userSubscriptionRepository::findByUserAndActiveTrue)
                .filter(sub -> sub.getExpiresAt().isAfter(LocalDateTime.now()));
    }
}
