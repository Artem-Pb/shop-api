package com.polybezev.shop.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserResponse {
    private Long id;
    private String email;
    private String role;
}
