
package com.parkease.repository;

import com.parkease.entity.ParkingSpot;
import com.parkease.entity.VehicleType;

import jakarta.persistence.LockModeType;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParkingSpotRepository
        extends JpaRepository<ParkingSpot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ParkingSpot s where s.id = :id")
    Optional<ParkingSpot> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select s
        from ParkingSpot s
        where s.available = true
          and (:vehicle is null or s.vehicleType = :vehicle)
          and (
              lower(s.city) like lower(concat('%', :q, '%'))
              or lower(s.area) like lower(concat('%', :q, '%'))
              or lower(s.name) like lower(concat('%', :q, '%'))
          )
        """)
    Page<ParkingSpot> search(
            @Param("q") String query,
            @Param("vehicle") VehicleType vehicleType,
            Pageable pageable
    );

    Page<ParkingSpot> findByOwnerId(
            Long ownerId,
            Pageable pageable
    );
}
