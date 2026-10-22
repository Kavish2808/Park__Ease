package com.parkease.controller;

import com.parkease.dto.*;
import com.parkease.service.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@AuthenticationPrincipal UserDetails u,
            @Valid @RequestBody BookingRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(u.getUsername(), r));
    }

    @GetMapping("/mine")
    public List<BookingResponse> mine(@AuthenticationPrincipal UserDetails u) {
        return service.mine(u.getUsername());
    }

    @GetMapping("/owner")
    public List<BookingResponse> owner(@AuthenticationPrincipal UserDetails u) {
        return service.forOwner(u.getUsername());
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@AuthenticationPrincipal UserDetails u, @PathVariable Long id) {
        return service.cancel(u.getUsername(), id);
    }
}
