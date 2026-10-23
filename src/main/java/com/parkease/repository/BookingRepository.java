
package com.parkease.repository;

import com.parkease.entity.Booking;
import com.parkease.entity.BookingStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    @Query("""
        select count(b) > 0
        from Booking b
        where b.parkingSpot.id = :spotId
          and b.status in :statuses
          and :start < b.endTime
          and :end > b.startTime
        """)
    boolean hasConflict(
            @Param("spotId") Long spotId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("statuses") Collection<BookingStatus> statuses
    );

    @Query("""
        select count(b) > 0
        from Booking b
        where b.user.id = :userId
          and b.status in :statuses
          and :start < b.endTime
          and :end > b.startTime
        """)
    boolean hasUserConflict(
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("statuses") Collection<BookingStatus> statuses
    );

    @Query("""
        select b
        from Booking b
        where b.parkingSpot.id = :spotId
          and b.status in :statuses
          and b.startTime < :to
          and b.endTime > :from
        order by b.startTime
        """)
    List<Booking> findActiveBetween(
            @Param("spotId") Long spotId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("statuses") Collection<BookingStatus> statuses
    );

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByParkingSpotOwnerIdOrderByStartTimeDesc(
            Long ownerId
    );

    boolean existsByParkingSpotId(Long spotId);
}
