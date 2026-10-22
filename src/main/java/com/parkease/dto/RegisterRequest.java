package com.parkease.dto;

import com.parkease.entity.Role;
import jakarta.validation.constraints.*;

public record RegisterRequest(@NotBlank @Size(max = 80) String name, @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password, Role role) {
}
