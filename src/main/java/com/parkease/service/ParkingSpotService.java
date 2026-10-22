package com.parkease.service;
import com.parkease.dto.*; import com.parkease.entity.*; import com.parkease.exception.ApiException; import com.parkease.repository.*; import org.springframework.data.domain.*; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @Transactional public class ParkingSpotService {
 private final ParkingSpotRepository spots; private final UserRepository users; private final BookingRepository bookings;
 public ParkingSpotService(ParkingSpotRepository spots,UserRepository users,BookingRepository bookings){this.spots=spots;this.users=users;this.bookings=bookings;}
 public Page<ParkingSpotResponse> search(String query,VehicleType vehicle,int page,int size,String sort){Sort order=switch(sort){case "price"->Sort.by("pricePerHour").ascending();case "price_desc"->Sort.by("pricePerHour").descending();default->Sort.by("id").ascending();};return spots.search(query==null?"":query.trim(),vehicle,PageRequest.of(page,Math.min(size,50),order)).map(this::map);}
 public ParkingSpotResponse get(Long id){return map(find(id));}
 public ParkingSpotResponse create(String email,ParkingSpotRequest req){User owner=user(email);if(owner.getRole()!=Role.OWNER&&owner.getRole()!=Role.ADMIN)throw new ApiException(HttpStatus.FORBIDDEN,"Only parking owners can add spaces");ParkingSpot s=new ParkingSpot();s.setOwner(owner);fill(s,req);return map(spots.save(s));}
 public ParkingSpotResponse update(String email,Long id,ParkingSpotRequest req){ParkingSpot s=find(id);verifyOwner(email,s);fill(s,req);return map(spots.save(s));}
 public void delete(String email,Long id){ParkingSpot s=find(id);verifyOwner(email,s);if(bookings.existsByParkingSpotId(id))throw new ApiException(HttpStatus.CONFLICT,"This space has bookings, so it cannot be deleted. Pause it instead.");spots.delete(s);}
 public Page<ParkingSpotResponse> mine(String email,int page,int size){return spots.findByOwnerId(user(email).getId(),PageRequest.of(page,Math.min(size,50))).map(this::map);}
 private void fill(ParkingSpot s,ParkingSpotRequest r){s.setName(r.name());s.setAddress(r.address());s.setCity(r.city());s.setArea(r.area());s.setPricePerHour(r.pricePerHour());s.setVehicleType(r.vehicleType());s.setAvailable(r.available());s.setImageUrl(r.imageUrl());}
 private ParkingSpot find(Long id){return spots.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Parking spot not found"));} private User user(String email){return users.findByEmail(email).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"User not found"));}
 private void verifyOwner(String email,ParkingSpot s){User u=user(email);if(u.getRole()!=Role.ADMIN&&!s.getOwner().getId().equals(u.getId()))throw new ApiException(HttpStatus.FORBIDDEN,"You do not own this parking spot");}
 private ParkingSpotResponse map(ParkingSpot s){return new ParkingSpotResponse(s.getId(),s.getOwner().getId(),s.getName(),s.getAddress(),s.getCity(),s.getArea(),s.getPricePerHour(),s.getVehicleType(),s.isAvailable(),s.getImageUrl());}
}
