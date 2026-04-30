package com.polybezev.shop.repository;

import com.polybezev.shop.entity.Cart;
import com.polybezev.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
