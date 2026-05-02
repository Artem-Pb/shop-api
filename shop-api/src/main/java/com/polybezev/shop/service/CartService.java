package com.polybezev.shop.service;

import com.polybezev.shop.entity.Cart;
import com.polybezev.shop.entity.CartItem;
import com.polybezev.shop.entity.Product;
import com.polybezev.shop.entity.User;
import com.polybezev.shop.exception.BadRequestException;
import com.polybezev.shop.exception.NotFoundException;
import com.polybezev.shop.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;

    @Transactional
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    public Cart getByUser(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Cart not found"));
    }

    public Cart addItem(Cart cart, Product product, Integer quantity) {
        if (quantity <= 0)
            throw new BadRequestException("Quantity must be greater than 0");

        if (product.getStock() < quantity)
            throw new BadRequestException("Not enough stock. Available: " + product.getStock());

        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> {
                            int newTotal = item.getQuantity() + quantity;
                            if (newTotal > product.getStock())
                                throw new BadRequestException("Not enough stock. Available: " + product.getStock());
                            item.setQuantity(newTotal);
                        },
                        () -> {
                            CartItem newCartItem = new CartItem();
                            newCartItem.setCart(cart);
                            newCartItem.setProduct(product);
                            newCartItem.setQuantity(quantity);
                            cart.getItems().add(newCartItem);
                        }
                );

        return cartRepository.save(cart);
    }

    public void removeItem(Cart cart, Long cartItemId) {
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        cart.getItems().remove(item);
        cartRepository.save(cart);
    }

    public Cart updateQuantity(Cart cart, Long cartItemId, Integer quantity) {
        if (quantity <= 0)
            throw new BadRequestException("Quantity must be greater than 0");

        CartItem cartItem = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        cartItem.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
