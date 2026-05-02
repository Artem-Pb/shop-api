package com.polybezev.shop.service;

import com.polybezev.shop.entity.Role;
import com.polybezev.shop.entity.User;
import com.polybezev.shop.exception.NotFoundException;
import com.polybezev.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User updateRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.setRole(role);
        return userRepository.save(user);
    }
}
