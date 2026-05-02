# Shop API — Context snapshot

## Entity

### Category.java
```java
package com.polybezev.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter @Setter @NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
```

### Role.java
```java
package com.polybezev.shop.entity;

public enum Role {
    ADMIN,
    USER
}
```

### User.java
```java
package com.polybezev.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
```

### Product.java
```java
package com.polybezev.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
```

### Cart.java
```java
package com.polybezev.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter @Setter @NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}
```

### CartItem.java
```java
package com.polybezev.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart_items")
@Getter @Setter @NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}
```

### OrderStatus.java
```java
package com.polybezev.shop.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
```

### Order.java
```java
package com.polybezev.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();
}
```

### OrderItem.java
```java
package com.polybezev.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private BigDecimal priceAtPurchase;

    @Column(nullable = false)
    private Integer quantity;
}
```

---

## Repository

### CategoryRepository.java
```java
package com.polybezev.shop.repository;

import com.polybezev.shop.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
}
```

### ProductRepository.java
```java
package com.polybezev.shop.repository;

import com.polybezev.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByStockGreaterThan(Integer stock, Pageable pageable);
    Page<Product> findByCategoryIdAndStockGreaterThan(Long categoryId, Integer stock, Pageable pageable);
}
```

### CartRepository.java
```java
package com.polybezev.shop.repository;

import com.polybezev.shop.entity.Cart;
import com.polybezev.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
```

### OrderRepository.java
```java
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
```

### UserRepository.java
```java
package com.polybezev.shop.repository;

import com.polybezev.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

---

## Service

### CategoryService.java
```java
package com.polybezev.shop.service;

import com.polybezev.shop.entity.Category;
import com.polybezev.shop.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found!"));
    }

    public Category create(Category request) {
        String name = request.getName() == null || request.getName().isBlank() ?
                null : request.getName().trim();

        if (name == null)
            throw new RuntimeException("Name is blank!");

        if (name.length() > 50)
            throw new RuntimeException("Over length!");

        if (categoryRepository.existsByNameIgnoreCase(name))
            throw new RuntimeException("Name already exists!");

        request.setName(name);
        return categoryRepository.save(request);
    }

    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        categoryRepository.delete(category);
    }
}
```

### ProductService.java
```java
package com.polybezev.shop.service;

import com.polybezev.shop.entity.Category;
import com.polybezev.shop.entity.Product;
import com.polybezev.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found!"));
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Page<Product> getAll(Long categoryId, Pageable page) {
        if (categoryId == null)
            return productRepository.findByStockGreaterThan(0, page);
        categoryService.getById(categoryId);
        return productRepository.findByCategoryIdAndStockGreaterThan(categoryId, 0, page);
    }

    public Product create(Product request) {
        String name = request.getName() == null || request.getName().isBlank() ?
                null : request.getName().trim();
        if (name == null)
            throw new RuntimeException("Name is empty!");

        request.setName(name);

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new RuntimeException("Price must be greater than 0");

        if (request.getStock() == null || request.getStock() <= 0)
            throw new RuntimeException("Stock must be greater than 0");

        if (request.getCategory() == null)
            throw new RuntimeException("Category is required!");

        Category category = categoryService.getById(request.getCategory().getId());
        request.setCategory(category);
        return productRepository.save(request);
    }

    @Transactional
    public void delete(Long id) {
        Product product = getById(id);
        productRepository.delete(product);
    }

    @Transactional
    public Product update(Product request) {
        if (request.getId() == null)
            throw new RuntimeException("Id is required!");

        Product product = getById(request.getId());

        if (request.getName() != null) {
            if (request.getName().isBlank())
                throw new RuntimeException("Name is empty!");
            product.setName(request.getName());
        }

        if (request.getDescription() != null)
            product.setDescription(request.getDescription());

        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0)
                throw new RuntimeException("Price must be greater than 0");
            product.setPrice(request.getPrice());
        }

        if (request.getStock() != null) {
            if (request.getStock() <= 0)
                throw new RuntimeException("Stock must be greater than 0");
            product.setStock(request.getStock());
        }

        return productRepository.save(product);
    }
}
```

### CartService.java
```java
package com.polybezev.shop.service;

import com.polybezev.shop.entity.Cart;
import com.polybezev.shop.entity.CartItem;
import com.polybezev.shop.entity.Product;
import com.polybezev.shop.entity.User;
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
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    public Cart addItem(Cart cart, Product product, Integer quantity) {
        if (quantity <= 0)
            throw new RuntimeException("Quantity must be greater than 0");

        if (product.getStock() < quantity)
            throw new RuntimeException("Not enough stock. Available: " + product.getStock());

        cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
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
                .orElseThrow(() -> new RuntimeException("Item not found!"));

        cart.getItems().remove(item);
        cartRepository.save(cart);
    }

    public Cart updateQuantity(Cart cart, Long cartItemId, Integer quantity) {
        if (quantity <= 0)
            throw new RuntimeException("Quantity must be greater than 0");

        CartItem cartItem = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found!"));

        cartItem.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
```

### OrderService.java
```java
package com.polybezev.shop.service;

import com.polybezev.shop.entity.*;
import com.polybezev.shop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

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
                throw new RuntimeException("Not enough stock: " + item.getProduct().getName());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductName(item.getProduct().getName());
            orderItem.setPriceAtPurchase(item.getProduct().getPrice());
            orderItem.setQuantity(item.getQuantity());

            order.getOrderItems().add(orderItem);
            total = total.add(orderItem.getPriceAtPurchase()
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
            item.getProduct().setStock(item.getProduct().getStock() - item.getQuantity());
            productService.save(item.getProduct());
        }

        order.setTotalAmount(total);
        cartService.clearCart(cart);
        return orderRepository.save(order);
    }

    // TODO: getById, getByUser, updateStatus
}
```

---

## Что осталось сделать

- [ ] `OrderService` — дописать `getById`, `getByUser`, `updateStatus`
- [ ] `Controller` слой — Category, Product, Cart, Order
- [ ] `Security` слой — JWT, Spring Security, AuthController
- [ ] `Exception` слой — кастомные исключения, GlobalExceptionHandler
- [ ] `application.yml` — финальная конфигурация
- [ ] `Docker Compose` — PostgreSQL + app
- [ ] `README.md`
