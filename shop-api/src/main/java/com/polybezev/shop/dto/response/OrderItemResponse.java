package com.polybezev.shop.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class OrderItemResponse {
    private Long id;
    private String productName;
    private BigDecimal priceAtPurchase;
    private Integer quantity;
}
