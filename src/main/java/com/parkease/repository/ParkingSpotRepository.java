package com.parkease.repository;
import com.parkease.entity.*; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
public interface ParkingSpotRepository extends JpaRepository<ParkingSpot,Long>{
 @Query("select s from ParkingSpot s where s.available=true and (:vehicle is null or s.vehicleType=:vehicle) and (lower(s.city) like lower(concat('%',:q,'%')) or lower(s.area) like lower(concat('%',:q,'%')) or lower(s.name) like lower(concat('%',:q,'%')))") Page<ParkingSpot> search(@Param("q") String query, @Param("vehicle") VehicleType vehicleType, Pageable pageable);
 Page<ParkingSpot> findByOwnerId(Long ownerId, Pageable pageable);
}
