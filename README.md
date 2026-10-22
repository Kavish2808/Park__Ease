# 🚗 ParkEase - Smart Parking Marketplace

A full-stack parking booking platform built using **Spring Boot** and **React**, where parking owners can list their parking spaces and users can search, book, and pay for parking slots.

> **Project Goal:** Build an industry-level backend with clean architecture and real-world business logic that is suitable for placements and portfolio.

---

# 📌 Project Scope

This project aims to achieve approximately **80-85%** of a real-world parking booking application without adding unnecessary complexity.

The focus is on:

- Clean Code
- Scalable Architecture
- REST APIs
- Security
- Database Design
- Business Logic
- Interview Ready Implementation

---

# 🛠 Tech Stack

## Backend

- Java 21
- Spring Boot 3
- Spring Security
- JWT Authentication
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Lombok
- ModelMapper
- Bean Validation
- Swagger (SpringDoc)

---

## Frontend (Later)

- React
- Axios
- React Router
- Tailwind CSS

---

# 📂 Project Architecture

```
React Frontend
       │
 REST APIs (HTTP)
       │
Spring Boot Backend
       │
 Controller
       │
 Service
       │
 Repository
       │
    MySQL
```

---

# 📦 Planned Package Structure

```
com.parkease

├── config
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── repository
├── service
│   └── impl
├── security
├── exception
├── mapper
├── util
└── ParkeaseApplication
```

---

# 🚀 Features

## Authentication

- User Registration
- Login
- JWT Authentication
- Role-Based Authorization

Roles:

- USER
- OWNER
- ADMIN

---

## User Module

- Update Profile
- View Profile
- Booking History
- Payment History

---

## Parking Spot Module

Owner can

- Add Parking Spot
- Update Parking Spot
- Delete Parking Spot
- View Own Parking Spots

Parking Details

- Address
- City
- Price Per Hour
- Vehicle Type
- Availability
- Images

---

## Search Module

Search by

- City
- Area
- Date
- Time
- Vehicle Type
- Price

Additional Features

- Pagination
- Sorting

---

## Booking Module

- Book Parking Spot
- Cancel Booking
- Booking History
- Booking Status

Statuses

- Pending
- Confirmed
- Cancelled
- Completed

---

## Payment Module

- Dummy Payment
- Payment Status
- Payment History

---

## Review Module

- Give Rating
- Review Parking Spot
- Average Rating

---

## Owner Dashboard

- Total Earnings
- Total Bookings
- Active Listings
- Recent Bookings

---

## Admin Module

- Manage Users
- Manage Parking Spots
- View All Bookings

---

# ⭐ Core Feature

## Double Booking Prevention

The most important business logic in the project.

Example

Existing Booking

```
11:00 AM - 1:00 PM
```

Requested Booking

```
12:00 PM - 2:00 PM
```

Booking should be rejected because both bookings overlap.

Logic

```java
if (requestedStart < existingEnd &&
    requestedEnd > existingStart) {
    throw new BookingConflictException();
}
```

---

# 🗄 Database Tables

```
User
ParkingSpot
Booking
Payment
Review
```

Optional

```
ParkingImage
```

---

# 🏗 Development Phases

## ✅ Phase 1

Project Setup

- Spring Boot Project
- Dependencies
- Package Structure
- MySQL Configuration
- Application Properties

---

## Phase 2

Database Design

- Entity Relationship Diagram
- User Entity
- ParkingSpot Entity
- Booking Entity
- Payment Entity
- Review Entity

---

## Phase 3

Authentication

- Registration
- Login
- JWT
- Spring Security
- Roles

---

## Phase 4

Parking CRUD

- Add Parking
- Update Parking
- Delete Parking
- Get Parking Details

---

## Phase 5

Search APIs

- Filters
- Pagination
- Sorting

---

## Phase 6

Booking Module

- Book Parking
- Cancel Booking
- Conflict Detection

---

## Phase 7

Payment Module

- Dummy Payment
- Payment Status
- Payment History

---

## Phase 8

Dashboard

Owner Dashboard

Admin Dashboard

Swagger Documentation

Validation

Testing

---

# 📚 Development Workflow

Every feature will be implemented using the same process.

1. Requirement Analysis
2. Database Design
3. Entity Creation
4. DTO Creation
5. Repository
6. Service
7. Controller
8. API Testing
9. Git Commit

---

# 📋 Best Practices

- Layered Architecture
- DTO Pattern
- Global Exception Handling
- Validation
- JWT Security
- REST API Standards
- Proper HTTP Status Codes
- Clean Package Structure
- ModelMapper
- Swagger Documentation
- Pagination
- Meaningful Naming Conventions

---

# ❌ Features Intentionally Excluded

To keep the project realistic and finishable, the following features are excluded.

- Google Maps API
- Live GPS Tracking
- AI Recommendations
- Dynamic Pricing
- Razorpay Integration
- Email OTP
- Notifications
- Wishlist
- Charts & Analytics
- Microservices
- Docker & Kubernetes

These can be added later as future enhancements.

---

# 🎯 Learning Goals

By the end of this project, you should be able to explain:

- Spring Boot Project Structure
- REST API Design
- Spring Security
- JWT Authentication
- JPA Relationships
- DTO Pattern
- Exception Handling
- Validation
- Booking Conflict Detection
- Layered Architecture
- CRUD Operations
- Pagination & Filtering

---

# 📈 Project Status

| Phase | Status |
|--------|--------|
| Project Setup | ⏳ |
| Database Design | ⏳ |
| Authentication | ⏳ |
| Parking Module | ⏳ |
| Search Module | ⏳ |
| Booking Module | ⏳ |
| Payment Module | ⏳ |
| Dashboard | ⏳ |
| Testing | ⏳ |

---

# 📌 Current Progress

### Completed

- Project Planning
- Feature Finalization
- Architecture Design
- Package Structure
- Development Roadmap

### Next Step

➡️ **Phase 1 - Spring Boot Project Setup**

---

# 🤝 Project Philosophy

This project is being developed step-by-step, following industry standards rather than simply creating CRUD APIs.

The goal is to build a backend that demonstrates strong software engineering practices and can be confidently explained during technical interviews and showcased in a professional portfolio.