# Hotel Reservation Manager

A full-stack hotel reservation management application built for Ionio Gastronomy Suites. Guests can browse rooms, make bookings, and track reservations using a confirmation code. Administrators have a dedicated panel to manage rooms and bookings.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Backend](#backend)
- [Frontend](#frontend)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Project Structure](#project-structure)

---

## Overview

Hotel Reservation Manager is a full-stack web application with a React frontend and a Spring Boot backend. Users can register and log in, search for available rooms by date and type, make bookings, and retrieve booking details using a confirmation code. Administrators can manage the full inventory of rooms and bookings through a protected admin panel.

---

## Features

### Guest / User
- Register and log in with JWT-based authentication
- Browse all rooms with pagination and filter by room type
- Search for available rooms by check-in date, check-out date, and room type
- View detailed room information including existing booking dates
- Make a room booking and receive a unique alphanumeric confirmation code
- Look up an existing booking by confirmation code
- View personal profile and full booking history with room details
- Delete personal account

### Admin
- Access a protected admin dashboard showing the admin's name
- Add new rooms with photo upload (stored on AWS S3), type, price, and description
- Select an existing room type from a dropdown or define a new custom type
- Edit room details and replace room photos
- Delete rooms
- View all bookings with search by confirmation code and pagination
- View full booking details including guest and room information
- Cancel (archive) any booking

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, React Router v6, Axios, React DatePicker |
| Backend | Java 25, Spring Boot 4.0.4, Spring Security, Spring Data JPA |
| Authentication | JWT via JJWT 0.13.0 |
| Database | MySQL (via mysql-connector-j) |
| File Storage | AWS SDK for Java v2 (S3) |
| Validation | Spring Boot Validation (jakarta.validation) |
| Utilities | Lombok |
| Build Tool | Maven |

---

## Backend

The backend is a Spring Boot REST API located in the `backend/` directory.

### Architecture

The backend follows a layered architecture:

- **Controllers** handle incoming HTTP requests and delegate to services
- **Services** contain business logic and interact with repositories
- **Repositories** extend Spring Data JPA for database access
- **Entities** are JPA-mapped domain objects (`Room`, `Booking`, `User`)
- **DTOs** are used for all API responses to avoid exposing entities directly
- **Response** is a unified response wrapper used by all endpoints
- **Utils** contains entity-to-DTO mapping helpers and a confirmation code generator
- **JWTUtils** handles token generation and validation

### API Endpoints

#### Authentication
| Method | Endpoint | Access |
|---|---|---|
| POST | `/auth/register` | Public |
| POST | `/auth/login` | Public |

#### Rooms
| Method | Endpoint | Access |
|---|---|---|
| GET | `/rooms/all` | Public |
| GET | `/rooms/types` | Public |
| GET | `/rooms/room-by-id/{roomId}` | Public |
| GET | `/rooms/all-available-rooms` | Public |
| GET | `/rooms/available-rooms-by-date-and-type?checkInDate=&checkOutDate=&roomType=` | Public |
| POST | `/rooms/add` | Admin |
| PUT | `/rooms/update/{roomId}` | Admin |
| DELETE | `/rooms/delete/{roomId}` | Admin |

#### Bookings
| Method | Endpoint | Access |
|---|---|---|
| POST | `/bookings/book-room/{roomId}/{userId}` | User, Admin |
| GET | `/bookings/all` | Admin |
| GET | `/bookings/get-by-confirmation-code/{confirmationCode}` | Public |
| DELETE | `/bookings/cancel/{bookingId}` | User, Admin |

#### Users
| Method | Endpoint | Access |
|---|---|---|
| GET | `/users/all` | Admin |
| GET | `/users/get-logged-in-profile-info` | Authenticated |
| GET | `/users/get-by-id/{userId}` | Authenticated |
| GET | `/users/get-user-bookings/{userId}` | Authenticated |
| DELETE | `/users/delete/{userId}` | Authenticated |

### Security

- JWT tokens are issued on login and validated on every protected request via a filter
- Tokens are valid for 7 days
- Role-based access is enforced using Spring Security `@PreAuthorize` with `ADMIN` and `USER` authorities
- The logged-in user's identity is resolved from the Spring `SecurityContextHolder` using the email embedded in the token
- The JWT secret is externalized to `application.properties` and injected via `@Value` with `@PostConstruct` initialization

### Booking Availability Logic

When a booking request is made, the backend retrieves all existing bookings for that room and checks for date range overlap. A conflict is detected when the requested check-in date is before an existing check-out date and the existing check-in date is before the requested check-out date. If any conflict is found the booking is rejected with a 404 response.

### Confirmation Code

Each confirmed booking receives a randomly generated 10-character alphanumeric confirmation code using `SecureRandom`. This code is used to retrieve and manage the booking from the frontend.

---

## Frontend

The frontend is a React single-page application located in `frontend/emil-hotel-react/`.

### Pages and Routing

| Route | Component | Access |
|---|---|---|
| `/home` | HomePage | Public |
| `/login` | LoginPage | Public |
| `/register` | RegisterPage | Public |
| `/rooms` | AllRoomsPage | Public |
| `/find-booking` | FindBookingPage | Public |
| `/room-details-book/:roomId` | RoomDetailsBookingPage | Protected |
| `/profile` | ProfilePage | Protected |
| `/edit-profile` | EditProfilePage | Protected |
| `/admin` | AdminPage | Admin |
| `/admin/manage-rooms` | ManageRoomPage | Admin |
| `/admin/add-room` | AddRoomPage | Admin |
| `/admin/edit-room/:roomId` | EditRoomPage | Admin |
| `/admin/manage-bookings` | ManageBookingsPage | Admin |
| `/admin/edit-booking/:bookingCode` | EditBookingPage | Admin |

Any unmatched route redirects to `/login`.

### Route Guards

Two guard components in `Guard.js` protect routes based on authentication state and role:

- `ProtectedRoute` — redirects unauthenticated users to `/login`, preserving the intended destination in router state
- `AdminRoute` — redirects non-admin users to `/login`

Both guards read authentication state directly from `ApiService`, which checks `localStorage` for a token and role.

### Key Components

- `Navbar` — renders navigation links dynamically based on authentication state and role. Shows Profile for users, Admin for admins, Login/Register for guests, and a Logout option for authenticated users
- `Footer` — displays the hotel name, copyright notice, and the current year dynamically
- `RoomSearch` — date range and room type search form that calls the availability endpoint and passes results up via a callback prop
- `RoomResult` — displays a paginated list of rooms; renders an Edit Room button for admins and a View/Book Now button for regular users
- `Pagination` — reusable page number component shared between room and booking list pages
- `BookingResult` — displays booking summary cards in the admin panel

### ApiService

All HTTP communication is handled through the static `ApiService` class using Axios. It reads the JWT token from `localStorage` and attaches it as a `Bearer` authorization header on all authenticated requests. Authentication helper methods (`isAuthenticated`, `isAdmin`, `isUser`, `logout`) are also provided here and used throughout the app for conditional rendering and route protection.

### Profile Page

The profile page makes two sequential API calls: first to get the logged-in user's basic info, then to fetch that user's full booking history including room details. Each booking in the history displays the confirmation code, check-in and check-out dates, total guests, room type, and a room photo.

### Edit Profile Page

Currently allows users to view their profile details and permanently delete their account. On deletion the user is redirected to `/register`.

---

## Getting Started

### Prerequisites

- Node.js 18 or higher
- Java 25
- Maven
- MySQL
- An AWS S3 bucket for room photo storage

### Backend Setup

```bash
cd backend
# Create and configure application.properties (see Environment Variables below)
mvn spring-boot:run
```

The API starts on `http://localhost:4040`.

### Frontend Setup

```bash
cd frontend/emil-hotel-react
npm install
npm start
```

The React app starts on `http://localhost:3000`.

Static assets such as the homepage banner image should be placed in `frontend/emil-hotel-react/public/assets/images/`.

---

## Environment Variables

Create `backend/src/main/resources/application.properties` based on the following template. This file must not be committed to version control — add it to `.gitignore` and commit an `application.properties.example` instead.

```properties
# Server
server.port=4040

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=your_base64_encoded_secret

# AWS S3
aws.s3.bucket.name=your_bucket_name
aws.s3.access.key=your_access_key
aws.s3.secret.key=your_secret_key
aws.s3.region=your_region
```

---

## Project Structure

```
Hotel-Reservation-Manager/
├── backend/
│   └── src/main/java/com/Emil/HotelManagement/
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── BookingController.java
│       │   ├── RoomController.java
│       │   └── UserController.java
│       ├── dto/
│       │   ├── BookingDTO.java
│       │   ├── Response.java
│       │   ├── RoomDTO.java
│       │   └── UserDTO.java
│       ├── entity/
│       │   ├── Booking.java
│       │   ├── Room.java
│       │   └── User.java
│       ├── exception/
│       │   └── MyException.java
│       ├── repo/
│       │   ├── BookingRepository.java
│       │   ├── RoomRepository.java
│       │   └── UserRepository.java
│       ├── service/
│       │   ├── impl/
│       │   │   ├── BookingService.java
│       │   │   ├── RoomService.java
│       │   │   └── UserService.java
│       │   └── interfac/
│       │       ├── IBookingService.java
│       │       ├── IRoomService.java
│       │       └── IUserService.java
│       ├── service/
│       │   └── AwsS3Service.java
│       └── utils/
│           ├── JWTUtils.java
│           └── Utils.java
│
└── frontend/emil-hotel-react/
    ├── public/
    │   └── assets/images/        # Static assets (banner image, service icons)
    └── src/
        ├── component/
        │   ├── admin/
        │   │   ├── AdminPage.jsx
        │   │   ├── AddRoomPage.jsx
        │   │   ├── EditBookingPage.jsx
        │   │   ├── EditRoomPage.jsx
        │   │   ├── ManageBookingsPage.jsx
        │   │   └── ManageRoomPage.jsx
        │   ├── auth/
        │   │   ├── LoginPage.jsx
        │   │   └── RegisterPage.jsx
        │   ├── booking_rooms/
        │   │   ├── AllRoomsPage.jsx
        │   │   ├── FindBookingPage.jsx
        │   │   └── RoomDetailsPage.jsx
        │   ├── common/
        │   │   ├── BookingResult.jsx
        │   │   ├── Footer.jsx
        │   │   ├── Navbar.jsx
        │   │   ├── Pagination.jsx
        │   │   ├── RoomResult.jsx
        │   │   └── RoomSearch.jsx
        │   ├── home/
        │   │   └── HomePage.jsx
        │   └── profile/
        │       ├── EditProfilePage.jsx
        │       └── ProfilePage.jsx
        └── service/
            ├── ApiService.js
            └── Guard.js
```
