package com.parkease.dto;

import com.parkease.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record BookingResponse(Long id, Long parkingSpotId, String parkingSpotName, String address, String area,
        String city, String userName, Instant startTime, Instant endTime, BigDecimal totalAmount,
        BookingStatus status) {
}
