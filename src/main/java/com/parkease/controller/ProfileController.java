package com.parkease.controller;

import com.parkease.entity.User;
import com.parkease.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final UserRepository users;

    public ProfileController(UserRepository users) {
        this.users = users;
    }

    @GetMapping
    public ProfileResponse profile(@AuthenticationPrincipal UserDetails principal) {
        User u = users.findByEmail(principal.getUsername())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return new ProfileResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name());
    }

    public record ProfileResponse(Long id, String name, String email, String role) {
    }
}
