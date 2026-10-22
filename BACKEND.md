# ParkEase API

## Run

1. Create/start MySQL and ensure the configured user can create or access the `parkease` database. The supplied local connection URL enables `allowPublicKeyRetrieval` for MySQL 8's default authentication; use TLS and remove it for a production deployment.
2. Set database credentials as environment variables. The default is MySQL user `root` with no password, which matches common local installations. If your MySQL root account has a password, set it before starting PowerShell:

   ```powershell
   $env:DB_USERNAME = "root"
   $env:DB_PASSWORD = "your-mysql-password"
   ```
3. Run `mvn spring-boot:run`.

Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`.

## Main endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/auth/register` | Create a USER or OWNER account |
| POST | `/api/auth/login` | Get a JWT access token |
| GET | `/api/parking-spots` | Search; `city` matches city, area or spot name. Also `vehicleType`, `page`, `size`, `sort=price\|price_desc\|id` |
| GET | `/api/parking-spots/{id}/booked-slots` | Public. Booked intervals for a spot between `from` and `to` (ISO instants, max 7 days) |
| POST | `/api/parking-spots` | Create an owner parking space |
| PUT / DELETE | `/api/parking-spots/{id}` | Update or delete your space; delete is refused (409) once the space has bookings |
| GET | `/api/parking-spots/mine` | List the owner's spaces |
| POST | `/api/bookings` | Reserve a time slot (max 12 hours); rejects overlapping active bookings |
| GET | `/api/bookings/mine` | List current user's bookings; past confirmed bookings are returned as `COMPLETED` |
| GET | `/api/bookings/owner` | Owners only. Bookings made on the owner's spaces, with the driver's name |
| PATCH | `/api/bookings/{id}/cancel` | Cancel a booking that has not started yet |

Send the token from login as `Authorization: Bearer <token>` for authenticated requests.
