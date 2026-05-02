package com.polybezev.shop.service;

import com.polybezev.shop.entity.*;
import com.polybezev.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;

    @Transactional
    public Order createOrder(User user) {
        Cart cart = cartService.getByUser(user);
        if (cart.getItems().isEmpty())
            throw new RuntimeException("Cart is empty!");

        Order order = new Order();
        order.setUser(user);
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getStock() < item.getQuantity())
                throw new RuntimeException("Not enough stock: "
                        + item.getProduct().getName());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductName(item.getProduct().getName());
            orderItem.setPriceAtPurchase(item.getProduct().getPrice());
            orderItem.setQuantity(item.getQuantity());

            order.getOrderItems().add(orderItem);
            total = total.add(orderItem.getPriceAtPurchase().multiply(
                    BigDecimal.valueOf(item.getQuantity())));
            item.getProduct().setStock(item.getProduct().getStock()
                    - item.getQuantity());
            productService.save(item.getProduct());
        }

        order.setTotalAmount(total);
        cartService.clearCart(cart);
        return orderRepository.save(order);
    }

    public Order getById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order is found!"));
    }

    public List<Order> findByUser(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = getById(orderId);
        if (order.getOrderStatus().equals(OrderStatus.DELIVERED) || order.getOrderStatus().equals(OrderStatus.CANCELLED))
            throw new RuntimeException("Order is already " + order.getOrderStatus());
        order.setOrderStatus(newStatus);
        return orderRepository.save(order);
    }
}
