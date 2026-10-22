package com.parkease.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Booking {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) private User user;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) private ParkingSpot parkingSpot;
 @Column(nullable=false) private Instant startTime;
 @Column(nullable=false) private Instant endTime;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal totalAmount;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private BookingStatus status=BookingStatus.CONFIRMED;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
 public Long getId(){return id;} public User getUser(){return user;} public void setUser(User v){user=v;} public ParkingSpot getParkingSpot(){return parkingSpot;} public void setParkingSpot(ParkingSpot v){parkingSpot=v;} public Instant getStartTime(){return startTime;} public void setStartTime(Instant v){startTime=v;} public Instant getEndTime(){return endTime;} public void setEndTime(Instant v){endTime=v;} public BigDecimal getTotalAmount(){return totalAmount;} public void setTotalAmount(BigDecimal v){totalAmount=v;} public BookingStatus getStatus(){return status;} public void setStatus(BookingStatus v){status=v;} public Instant getCreatedAt(){return createdAt;}
}
