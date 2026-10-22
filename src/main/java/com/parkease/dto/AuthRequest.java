package com.parkease.dto;

import jakarta.validation.constraints.*;

public record AuthRequest(@NotBlank @Email String email, @NotBlank @Size(min = 8, max = 72) String password) {
}
