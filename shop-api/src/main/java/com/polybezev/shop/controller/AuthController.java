package com.polybezev.shop.controller;

import com.polybezev.shop.dto.request.LoginRequest;
import com.polybezev.shop.dto.request.RegisterRequest;
import com.polybezev.shop.dto.response.AuthResponse;
import com.polybezev.shop.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = new AuthResponse();
        response.setToken(authService.register(request));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = new AuthResponse();
        response.setToken(authService.login(request));
        return ResponseEntity.ok(response);
    }
}
