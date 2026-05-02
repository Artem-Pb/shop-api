package com.polybezev.shop.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class CartResponse {
    private Long id;
    private List<CartItemResponse> items;
}
