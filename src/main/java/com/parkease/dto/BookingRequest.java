package com.parkease.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record BookingRequest(@NotNull Long parkingSpotId, @NotNull @Future Instant startTime,
        @NotNull @Future Instant endTime) {
}
