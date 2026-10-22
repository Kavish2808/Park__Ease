package com.parkease.dto;

import com.parkease.entity.VehicleType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ParkingSpotRequest(@NotBlank String name, @NotBlank String address, @NotBlank String city,
        @NotBlank String area, @NotNull @DecimalMin("0.01") BigDecimal pricePerHour, @NotNull VehicleType vehicleType,
        boolean available, String imageUrl) {
}
