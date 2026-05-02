package com.polybezev.shop.repository;

import com.polybezev.shop.entity.Order;
import com.polybezev.shop.entity.OrderStatus;
import com.polybezev.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByOrderStatus(OrderStatus status);
}
