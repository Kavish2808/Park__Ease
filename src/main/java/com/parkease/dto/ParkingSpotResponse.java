package com.parkease.dto;

import com.parkease.entity.VehicleType;
import java.math.BigDecimal;

public record ParkingSpotResponse(Long id, Long ownerId, String name, String address, String city, String area,
        BigDecimal pricePerHour, VehicleType vehicleType, boolean available, String imageUrl) {
}
