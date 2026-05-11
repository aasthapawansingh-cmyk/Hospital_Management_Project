# Hospital Management System

## Overview
Hospital Management System is a Spring Boot backend application for managing patients, doctors, and appointments. The project includes JWT authentication, refresh token support, role-based authorization, validation, exception handling, AOP logging, and unit testing.

## Features
- Patient CRUD operations
- Doctor management
- Appointment booking
- JWT authentication
- Refresh token flow
- Role-based access control
- DTO-based request handling
- Booking conflict validation
- Appointment status transition logic
- Global exception handling
- AOP logging
- Unit testing
- Swagger API documentation

## Tech Stack
- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- Spring Validation
- Spring AOP
- JWT
- MySQL
- Maven
- Swagger / OpenAPI
- JUnit 5
- Mockito

## Authentication And Authorization
The project uses JWT-based authentication for protected APIs.

### Auth Endpoints
- `POST /auth/login`
- `POST /auth/refresh`

### Auth Flow
1. User logs in using username and password
2. Server returns:
   - `token`
   - `refreshToken`
3. The access token is sent in the `Authorization` header for protected requests
4. The refresh token is used to generate a new access token when the current one expires

Example header:

```http
Authorization: Bearer your_jwt_token
```

## Roles
The system currently defines these roles:
- ADMIN
- DOCTOR
- PATIENT

### Current Access Rules
- ADMIN can manage patient, doctor, and appointment records
- DOCTOR can access read operations on allowed modules
- PATIENT exists in authentication data but currently has no dedicated controller permissions

## API Modules

### Auth Module
- POST /auth/login
- POST /auth/refresh

### Patient Module
- POST /api/v1/patient
- GET /api/v1/patient
- GET /api/v1/patient/{id}
- PUT /api/v1/patient/{id}
- DELETE /api/v1/patient/{id}

### Doctor Module
- POST /api/v1/doctor
- GET /api/v1/doctor
- GET /api/v1/doctor/{id}
- DELETE /api/v1/doctor/{id}

### Appointment Module
- POST /api/v1/appointment
- GET /api/v1/appointment
- GET /api/v1/appointment/{id}
- DELETE /api/v1/appointment/{id}

## DTO Usage
The project uses DTOs to structure request data more cleanly.

Examples include:
- PatientReqDTO
- AppointmentRequestDTO
- LoginRequest
- RefreshTokenRequest

## Validation And Business Rules
The application includes validation and business rules to maintain clean and safe data handling.

### Patient Validation
Patient request data is validated for:
- required name
- age greater than 0
- required gender
- contact length of exactly 10 characters

### Appointment Booking Rules
- an appointment cannot be booked if the doctor already has another appointment at the same date and time
- patient and doctor must exist before an appointment can be created

## Appointment Status Flow
The appointment service contains status transition logic.

### Supported Status Values
- PENDING
- CONFIRMED
- COMPLETED
- CANCELLED

### Allowed Transitions
- PENDING -> CONFIRMED
- PENDING -> CANCELLED
- CONFIRMED -> COMPLETED
- CONFIRMED -> CANCELLED

Invalid transitions throw a custom exception.

Note: the status transition logic is implemented in the service layer.

## Exception Handling
The project uses custom exceptions and a global exception handler for cleaner API responses.

### Custom Exceptions Included
- PatientNotFoundException
- DoctorNotFoundException
- AppointmentNotFoundException
- AppointmentAlreadyBookedException
- InvalidAppointmentStatusException

These exceptions are handled centrally using GlobalExceptionHandler.

## Database Setup
This project uses MySQL.

For local development, create the database before running the application:

```sql
CREATE DATABASE hospital_db;
```

The datasource values can be configured in `src/main/resources/application.properties` or overridden with environment variables.

Default local values:
- database: `hospital_db`
- username: `root`
- password: `your-mysql-password`

## Logging
The project uses Spring AOP logging through LoggingAspect.

Current logging behavior:
- logs service method calls before execution

This helps track business-layer activity more clearly.

## Testing
The project includes unit tests for service-layer behavior.

### Covered Areas
- patient service retrieval
- successful appointment booking
- duplicate appointment prevention
- patient not found case
- doctor not found case
- valid appointment status transitions
- invalid appointment status transitions
- appointment not found during status update

## Seeded Users
The application seeds default users on startup for testing.
Credentials are configured via environment variables.

## How To Run

### Run Locally
1. Start MySQL
2. Create the `hospital_db` database
3. Update database username and password if needed
4. Run the application:

```bash
mvn spring-boot:run
```

### Run Tests

```bash
mvn test
```

### Run With Docker
Make sure Docker Desktop is running, then start the application and MySQL together:

```bash
docker compose up --build
```

This will start:
- MySQL on port `3306`
- Spring Boot application on port `8080`

Stop the containers with:

```bash
docker compose down
```

If you also want to remove the MySQL volume:

```bash
docker compose down -v
```

## Swagger Documentation
After the application starts, open:
- `http://localhost:8080/swagger-ui/index.html`

## Docker Notes
- The app container connects to MySQL using the service name `mysql`
- Datasource settings are passed through environment variables in `docker-compose.yml`
- `spring.jpa.hibernate.ddl-auto` is set to `update` for development convenience
- Change default credentials before using this setup outside local development
