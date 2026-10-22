package com.parkease.controller;

import com.parkease.dto.*;
import com.parkease.entity.VehicleType;
import com.parkease.service.BookingService;
import com.parkease.service.ParkingSpotService;
import java.time.Instant;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking-spots")
public class ParkingSpotController {
    private final ParkingSpotService service;
    private final BookingService bookings;

    public ParkingSpotController(ParkingSpotService service, BookingService bookings) {
        this.service = service;
        this.bookings = bookings;
    }

    @GetMapping
    public Page<ParkingSpotResponse> search(@RequestParam(defaultValue = "") String city,
            @RequestParam(required = false) VehicleType vehicleType, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size, @RequestParam(defaultValue = "id") String sort) {
        return service.search(city, vehicleType, page, size, sort);
    }

    @GetMapping("/{id}")
    public ParkingSpotResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping("/{id}/booked-slots")
    public List<BookedSlotResponse> bookedSlots(@PathVariable Long id, @RequestParam Instant from,
            @RequestParam Instant to) {
        return bookings.bookedSlots(id, from, to);
    }

    @PostMapping
    public ResponseEntity<ParkingSpotResponse> create(@AuthenticationPrincipal UserDetails u,
            @Valid @RequestBody ParkingSpotRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(u.getUsername(), r));
    }

    @PutMapping("/{id}")
    public ParkingSpotResponse update(@AuthenticationPrincipal UserDetails u, @PathVariable Long id,
            @Valid @RequestBody ParkingSpotRequest r) {
        return service.update(u.getUsername(), id, r);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetails u, @PathVariable Long id) {
        service.delete(u.getUsername(), id);
    }

    @GetMapping("/mine")
    public Page<ParkingSpotResponse> mine(@AuthenticationPrincipal UserDetails u,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.mine(u.getUsername(), page, size);
    }
}
