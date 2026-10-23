
package com.parkease.service;

import com.parkease.dto.*;
import com.parkease.entity.*;
import com.parkease.exception.ApiException;
import com.parkease.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookingService {

    private static final List<BookingStatus> ACTIVE = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private static final long MAX_BOOKING_HOURS = 12;

    private final BookingRepository bookings;
    private final ParkingSpotRepository spots;
    private final UserRepository users;

    public BookingService(
            BookingRepository bookings,
            ParkingSpotRepository spots,
            UserRepository users) {
        this.bookings = bookings;
        this.spots = spots;
        this.users = users;
    }

    public BookingResponse create(String email, BookingRequest r) {

        if (!r.endTime().isAfter(r.startTime())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "End time must be after start time"
            );
        }

        if (Duration.between(r.startTime(), r.endTime())
                .toHours() > MAX_BOOKING_HOURS) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "A booking can be at most "
                            + MAX_BOOKING_HOURS + " hours"
            );
        }

        // Lock the driver, then the spot, before any other read so the
        // overlap checks below see bookings committed by concurrent requests.
        User driver = users.findByEmailForUpdate(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));

        ParkingSpot s = spots.findByIdForUpdate(r.parkingSpotId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Parking spot not found"
                ));

        if (!s.isAvailable()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Parking spot is unavailable"
            );
        }

        if (bookings.hasConflict(
                s.getId(),
                r.startTime(),
                r.endTime(),
                ACTIVE)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "This parking spot is already booked for the selected time"
            );
        }

        if (bookings.hasUserConflict(
                driver.getId(),
                r.startTime(),
                r.endTime(),
                ACTIVE)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "You already have a booking that overlaps this time"
            );
        }

        long minutes = Math.max(
                1,
                Duration.between(r.startTime(), r.endTime()).toMinutes()
        );

        BigDecimal amount = s.getPricePerHour()
                .multiply(BigDecimal.valueOf(minutes))
                .divide(
                        BigDecimal.valueOf(60),
                        2,
                        RoundingMode.HALF_UP
                );

        Booking b = new Booking();
        b.setUser(driver);
        b.setParkingSpot(s);
        b.setStartTime(r.startTime());
        b.setEndTime(r.endTime());
        b.setTotalAmount(amount);

        return map(bookings.save(b));
    }

    public List<BookingResponse> mine(String email) {
        return bookings.findByUserIdOrderByCreatedAtDesc(
                        user(email).getId()
                )
                .stream()
                .map(this::map)
                .toList();
    }

    public List<BookingResponse> forOwner(String email) {

        User owner = user(email);

        if (owner.getRole() != Role.OWNER
                && owner.getRole() != Role.ADMIN) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Only parking owners can view received bookings"
            );
        }

        return bookings
                .findByParkingSpotOwnerIdOrderByStartTimeDesc(owner.getId())
                .stream()
                .map(this::map)
                .toList();
    }

    public List<BookedSlotResponse> bookedSlots(
            Long spotId,
            Instant from,
            Instant to) {

        if (!to.isAfter(from)
                || Duration.between(from, to).toDays() > 7) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Choose a range of up to 7 days"
            );
        }

        if (!spots.existsById(spotId)) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Parking spot not found"
            );
        }

        return bookings.findActiveBetween(
                        spotId,
                        from,
                        to,
                        ACTIVE
                )
                .stream()
                .map(b -> new BookedSlotResponse(
                        b.getStartTime(),
                        b.getEndTime()
                ))
                .toList();
    }

    public BookingResponse cancel(String email, Long id) {

        Booking b = bookings.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"
                ));

        if (!b.getUser().getId().equals(user(email).getId())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this booking"
            );
        }

        if (b.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Booking is already cancelled"
            );
        }

        if (!b.getStartTime().isAfter(Instant.now())) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Bookings that have already started cannot be cancelled"
            );
        }

        b.setStatus(BookingStatus.CANCELLED);

        return map(bookings.save(b));
    }

    private User user(String email) {
        return users.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));
    }

    private BookingResponse map(Booking b) {

        ParkingSpot s = b.getParkingSpot();

        BookingStatus status =
                b.getStatus() == BookingStatus.CONFIRMED
                        && b.getEndTime().isBefore(Instant.now())
                        ? BookingStatus.COMPLETED
                        : b.getStatus();

        return new BookingResponse(
                b.getId(),
                s.getId(),
                s.getName(),
                s.getAddress(),
                s.getArea(),
                s.getCity(),
                b.getUser().getName(),
                b.getStartTime(),
                b.getEndTime(),
                b.getTotalAmount(),
                status
        );
    }
}
