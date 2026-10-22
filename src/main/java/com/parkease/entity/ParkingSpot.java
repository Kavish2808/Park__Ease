package com.parkease.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class ParkingSpot {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY, optional=false) private User owner;
 @Column(nullable=false,length=100) private String name;
 @Column(nullable=false,length=180) private String address;
 @Column(nullable=false,length=80) private String city;
 @Column(nullable=false,length=80) private String area;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal pricePerHour;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private VehicleType vehicleType;
 @Column(nullable=false) private boolean available=true;
 private String imageUrl;
 private Instant createdAt=Instant.now();
 public Long getId(){return id;} public User getOwner(){return owner;} public void setOwner(User v){owner=v;} public String getName(){return name;} public void setName(String v){name=v;} public String getAddress(){return address;} public void setAddress(String v){address=v;} public String getCity(){return city;} public void setCity(String v){city=v;} public String getArea(){return area;} public void setArea(String v){area=v;} public BigDecimal getPricePerHour(){return pricePerHour;} public void setPricePerHour(BigDecimal v){pricePerHour=v;} public VehicleType getVehicleType(){return vehicleType;} public void setVehicleType(VehicleType v){vehicleType=v;} public boolean isAvailable(){return available;} public void setAvailable(boolean v){available=v;} public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
}
