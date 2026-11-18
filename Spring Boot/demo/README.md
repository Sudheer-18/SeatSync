# SeatSync backend

Spring Boot 4 / Java 21 REST API. It uses the existing MySQL connection, Hibernate schema updates, MySQL-backed seat holds, BCrypt passwords, and signed HS256 JWTs.

## Run locally

1. Copy `.env.example` to `.env` and set your local MySQL host, database name, username, and password. Spring Boot loads this file when starting from this directory.
2. Set a random `JWT_SECRET` of at least 32 bytes and `CORS_ALLOWED_ORIGINS` for the frontend. Keep `.env` out of source control.
3. Optional first-admin bootstrap: set both `SEATSYNC_ADMIN_EMAIL` and `SEATSYNC_ADMIN_PASSWORD` (12+ characters). An existing account is never promoted automatically; assign `ADMIN` explicitly in the database if needed.
4. From this directory, in that same terminal, run `.\mvnw.cmd test`, then `.\mvnw.cmd spring-boot:run`.
5. In `D:\SeatSync\react_project`, run `npm run dev`. The frontend defaults to `http://localhost:8080`; set `VITE_API_URL` to override it.

## Deploy with Docker Compose

The repository root contains a Compose deployment for MySQL, the API, and the React frontend. In PowerShell, from the repository root, copy `.env.example` to `.env`, edit it, and replace every example secret with unique random values and the actual production URLs:

```powershell
Copy-Item .env.example .env
docker compose up --build -d
docker compose logs -f backend
```

For a local smoke test, open `http://localhost:3000`. MySQL data is persisted in the `seatsync_mysql_data` volume; do not remove it when updating containers.

For a public deployment, use a VPS or host that supports Docker Compose, point DNS for your frontend and API domains to it, and put an HTTPS reverse proxy (for example Caddy) in front of them. Set `FRONTEND_ORIGIN` to the exact HTTPS frontend origin and `PUBLIC_API_URL` to the HTTPS API URL before building, because Vite embeds the API URL in the frontend bundle. The Compose ports bind only to localhost; the reverse proxy on the VPS can route the frontend domain to port 3000 and the API domain to port 8080. Configure firewall rules so MySQL is not publicly reachable. Back up the MySQL volume regularly. Set `DEMO_DATA_ENABLED=false` and configure the admin bootstrap and payment/notification values in the host's secret manager or environment; never upload `.env` or paste secrets into source files. Deploying means updating DNS, TLS, firewall, and host secrets; `docker compose up` only starts the containers on the machine where it runs.

Demo data is enabled by `seatsync.demo-data.enabled=true`: it ensures the 10 pictured routes have trips daily for the next 30 days starting tomorrow, with 40-seat AC seater buses. Each startup fills missing trips without duplicating existing ones. Only the listed one-way routes are seeded; for example, Kakinada → Hyderabad is Aditya Travels. Search using an exact source, destination, and date starting tomorrow.

UPI payments use a manually verified flow: new bookings stay `PENDING_PAYMENT` for 15 minutes, customers submit a UPI transaction reference, and an admin verifies it before the booking becomes `CONFIRMED`. Set `seatsync.payment.upi-id` in `application.properties` after the merchant UPI ID is provided. A UPI link does not itself verify money received. Expired payments free their seats.

Booking checkout requires a contact phone number in E.164 format (for example, `+919876543210`). Status updates are sent to that number and the user's registered email after booking/payment transactions commit, for pending payment, confirmation, rejection, and expiry. Email uses SMTP and SMS uses Twilio; delivery is disabled until configured. To enable them, set `EMAIL_NOTIFICATIONS_ENABLED=true`, `EMAIL_FROM`, and `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, and `SMTP_PASSWORD`; for SMS set `SMS_NOTIFICATIONS_ENABLED=true`, `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, and `TWILIO_FROM`. Keep all credentials in environment variables or the backend `.env` file, never in source control. Failed message delivery is logged and does not roll back a booking or payment status.

Seat holds are stored in MySQL and expire after five minutes. Rate limits are maintained in backend memory and reset when the application restarts; deploy only a single backend instance unless the limiter is moved to shared storage.

## API

All errors use `{ timestamp, status, error, message, path }`. Lists use `{ content, page, size, totalElements, totalPages }`; `page` starts at 0 and `size` is 1–50 (default 10). All `/admin/**` routes require an `ADMIN` JWT. Customer-specific routes require a bearer JWT.

The uploaded [API specification](../../SeatSync%20Backend%20API%20Specification%20(28%20APIs).pdf) contains the detailed request and response examples and field rules.

| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/auth/register` | Public | Create USER account |
| POST | `/auth/login` | Public | Login; returns JWT |
| GET | `/auth/me` | User | Current account |
| GET | `/trips/search?from=&to=&date=&type=&sort=&page=&size=` | Public | Search trips; repeated `type` filters supported |
| GET | `/trips/{id}` | Public | Trip, bus and route details |
| GET | `/trips/{id}/seats` | Public | Seat map with booked/held/owned state |
| POST | `/trips/{id}/hold` | User | Atomically hold 1–6 seats for 300 seconds |
| DELETE | `/trips/{id}/hold` | User | Release owned seats; empty body releases all own holds for that trip |
| POST | `/bookings` | User | Create a pending UPI booking with `contactPhone`; requires UUID `Idempotency-Key` |
| GET | `/bookings/{id}/payment-instructions` | Owner | UPI ID/link, amount, and expiry |
| PUT | `/bookings/{id}/payment-reference` | Owner | Submit UPI transaction reference |
| GET | `/bookings/me?status=&page=&size=` | User | Current user's bookings |
| GET | `/bookings/{id}` | Owner | Booking details; other users receive 404 |
| DELETE | `/bookings/{id}` | Owner | Cancel and mock-refund up to 2 hours before departure |
| POST / GET | `/admin/buses` | Admin | Create bus and seats / paginated list |
| PUT / DELETE | `/admin/buses/{id}` | Admin | Update bus / delete bus without trips |
| POST / GET | `/admin/routes` | Admin | Create route / paginated list |
| PUT / DELETE | `/admin/routes/{id}` | Admin | Update route / delete route without trips |
| POST / GET | `/admin/trips` | Admin | Create trip / paginated list (optional `date`) |
| PUT / DELETE | `/admin/trips/{id}` | Admin | Update schedule/price / delete trip without bookings |
| GET | `/admin/bookings` | Admin | Filter bookings by trip, status and date range |
| POST | `/admin/bookings/{id}/verify-payment` | Admin | Verify submitted payment reference and confirm booking |
| POST | `/admin/bookings/{id}/reject-payment` | Admin | Reject submitted payment and release seats |
| GET | `/admin/stats` | Admin | Booking, revenue, trip, user and top-route statistics |
| POST | `/chat` | User | Database-backed trip help and confirmed booking-cancellation flow |
| GET | `/chat/history?limit=` | User | Recent chat messages, oldest first (limit 1–100) |
| GET | `/health` | Public | `{ "status": "UP" }` |

Seat holds are stored in `seat_holds`, uniquely keyed by `(trip_id, seat_id)`, and use database row locks to serialize holds and bookings for the same trip. Booking creation enforces a unique `(trip_id, seat_id)` constraint and requires an exact match with the user's active holds. Pending UPI bookings retain the seat rows until admin review, cancellation, or 15-minute expiry. The chat assistant uses local route/trip data and does not require an external LLM credential.

## Database tables and relationships

- `users`: unique email, role, BCrypt `password_hash`; legacy `password` is hashed and cleared at startup.
- `buses` → `seats`: one bus has generated seats; `(bus_id, seat_number)` is unique.
- `routes`: unique source/destination pair.
- `trips` → `buses`, `routes`: each trip references one bus and route.
- `bookings` → `users`, `trips`: idempotency key is unique; stores the booking contact phone.
- `booking_seats` → `bookings`, `trips`, `seats`: `(trip_id, seat_id)` is unique; cancellation removes these rows to free seats.
- `payments` → `bookings`: one mock payment/refund record per booking.
- `chat_messages` → `users`; `chat_pending_actions` stores short-lived cancellation confirmations.

Hibernate uses `ddl-auto=update`; no second database connection or separate schema database is created.

## Verification

`.\mvnw.cmd test` runs H2-backed context and integration tests for registration/login/JWT authorization, trip search, seat-map reads, and hold ownership/expiry.
