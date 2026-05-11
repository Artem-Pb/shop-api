package com.polybezev.currencybot.repository;

import com.polybezev.currencybot.model.User;
import com.polybezev.currencybot.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findByUserAndActiveTrue(User user);

    @Query("select s from UserSubscription s join fetch s.user where s.active = true and s.tier != 'FREE'")
    List<UserSubscription> findAllActivePaid();
}
