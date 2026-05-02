package com.polybezev.shop.controller;

import com.polybezev.shop.dto.response.UserResponse;
import com.polybezev.shop.entity.Role;
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
}
