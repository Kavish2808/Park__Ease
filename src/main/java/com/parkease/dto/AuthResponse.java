package com.parkease.dto;

import com.parkease.entity.Role;

public record AuthResponse(String token, Long userId, String name, String email, Role role) {
}
