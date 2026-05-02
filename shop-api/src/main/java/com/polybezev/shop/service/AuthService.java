package com.polybezev.shop.service;

import com.polybezev.shop.dto.request.LoginRequest;
import com.polybezev.shop.dto.request.RegisterRequest;
import com.polybezev.shop.entity.Role;
import com.polybezev.shop.entity.User;
import com.polybezev.shop.exception.ConflictException;
import com.polybezev.shop.exception.NotFoundException;
import com.polybezev.shop.repository.UserRepository;
import com.polybezev.shop.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already exists");

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);
        return jwtService.generateToken(user.getEmail());
    }

    public String login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        return jwtService.generateToken(user.getEmail());
    }
}
