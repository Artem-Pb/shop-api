package com.polybezev.shop.controller;

import com.polybezev.shop.dto.request.CartRequest;
import com.polybezev.shop.dto.response.CartItemResponse;
import com.polybezev.shop.dto.response.CartResponse;
import com.polybezev.shop.entity.Cart;
import com.polybezev.shop.entity.Product;
import com.polybezev.shop.entity.User;
import com.polybezev.shop.service.CartService;
import com.polybezev.shop.service.ProductService;
import com.polybezev.shop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getByEmail(userDetails.getUsername());
        Cart cart = cartService.getOrCreateCart(user);
        return ResponseEntity.ok(toResponse(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal UserDetails userDetails,
                                                 @Valid @RequestBody CartRequest request) {
        User user = userService.getByEmail(userDetails.getUsername());
        Cart cart = cartService.getOrCreateCart(user);
        Product product = productService.getById(request.getProductId());
        Cart updated = cartService.addItem(cart, product, request.getQuantity());
        return ResponseEntity.ok(toResponse(updated));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateQuantity(@AuthenticationPrincipal UserDetails userDetails,
                                                        @PathVariable Long itemId,
                                                        @Valid @RequestBody CartRequest request) {
        User user = userService.getByEmail(userDetails.getUsername());
        Cart cart = cartService.getOrCreateCart(user);
        Cart updated = cartService.updateQuantity(cart, itemId, request.getQuantity());
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@AuthenticationPrincipal UserDetails userDetails,
                                            @PathVariable Long itemId) {
        User user = userService.getByEmail(userDetails.getUsername());
        Cart cart = cartService.getOrCreateCart(user);
        cartService.removeItem(cart, itemId);
        return ResponseEntity.noContent().build();
    }

    private CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setItems(cart.getItems().stream().map(item -> {
            CartItemResponse itemResponse = new CartItemResponse();
            itemResponse.setId(item.getId());
            itemResponse.setProductId(item.getProduct().getId());
            itemResponse.setProductName(item.getProduct().getName());
            itemResponse.setPrice(item.getProduct().getPrice());
            itemResponse.setQuantity(item.getQuantity());
            return itemResponse;
        }).toList());
        return response;
    }
}
