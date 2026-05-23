package com.polybezev.shop.controller;

import com.polybezev.shop.dto.response.OrderItemResponse;
import com.polybezev.shop.dto.response.OrderResponse;
import com.polybezev.shop.dto.response.UserResponse;
import com.polybezev.shop.entity.Order;
import com.polybezev.shop.entity.Role;
import com.polybezev.shop.service.OrderService;
import com.polybezev.shop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = userService.getAll().stream()
                .map(u -> {
                    UserResponse r = new UserResponse();
                    r.setId(u.getId());
                    r.setEmail(u.getEmail());
                    r.setRole(u.getRole().name());
                    return r;
                }).toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                    @RequestParam Role role) {
        var user = userService.updateRole(id, role);
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setEmail(user.getEmail());
        r.setRole(user.getRole().name());
        return ResponseEntity.ok(r);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.findAll().stream()
                .map(this::toOrderResponse)
                .toList();
        return ResponseEntity.ok(orders);
    }

    private OrderResponse toOrderResponse(Order order) {
        OrderResponse r = new OrderResponse();
        r.setId(order.getId());
        r.setOrderStatus(order.getOrderStatus().name());
        r.setTotalAmount(order.getTotalAmount());
        r.setCreatedAt(order.getCreatedAt());
        r.setItems(order.getOrderItems().stream().map(item -> {
            OrderItemResponse ir = new OrderItemResponse();
            ir.setId(item.getId());
            ir.setProductName(item.getProductName());
            ir.setPriceAtPurchase(item.getPriceAtPurchase());
            ir.setQuantity(item.getQuantity());
            return ir;
        }).toList());
        return r;
    }
}
