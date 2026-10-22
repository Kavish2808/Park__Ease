package com.parkease.dto;

import java.time.Instant;

public record BookedSlotResponse(Instant startTime, Instant endTime) {
}
