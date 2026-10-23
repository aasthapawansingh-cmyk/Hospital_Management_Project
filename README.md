# CareSync

A role-based hospital management application with a Spring Boot REST API and a React frontend.

## Live deployment

- **Frontend:** [Hospital Management UI](https://hospital-frontend-beta-six.vercel.app)
- **Backend API:** [Render service](https://hospital-backend-fyo4.onrender.com)
- **Swagger UI:** [API documentation](https://hospital-backend-fyo4.onrender.com/swagger-ui/index.html)

The backend uses PostgreSQL. The frontend is deployed on Vercel and calls the Render backend.

## Features

- JWT login and refresh tokens with `ADMIN`, `DOCTOR`, and `PATIENT` roles.
- Role-specific dashboards and server-side record access checks.
- Patient appointment requests, availability checks, doctor assignment, and booking conflict protection.
- Appointment status workflow and audit history.
- Admin doctor activation/deactivation, patient and doctor management, user-account linking, and appointment scheduling.
- Doctor-only AI visit summary drafts. A draft is not saved automatically and requires clinician review.

## Technology

- Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security, Spring Validation
- PostgreSQL
- React, Vite, Axios, React Router
- Maven, JUnit 5, Mockito
- Groq-compatible chat completions API for clinician summary drafts

## Authentication

### Endpoints

- `POST /auth/login` — returns an access token and refresh token.
- `POST /auth/refresh` — exchanges a refresh token for a new access token.
- `POST /auth/logout` — revokes the user's refresh session.
- `GET /auth/me` — returns the authenticated username, role, and linked profile ID.

For protected API requests, send:

```http
Authorization: Bearer <access-token>
```

Roles are determined from the authenticated server-side account. The API enforces access to patient-owned data and doctor-assigned appointments on the server.

## Appointment workflow

Appointment statuses are `PENDING`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, and `CANCELLED`.

- Patients can request an appointment as `PENDING` and cancel their own pending request.
- Doctors can act only on appointments assigned to them. They can confirm or reject pending requests, start a confirmed visit, and complete an in-progress visit.
- Admins can create and schedule appointments, confirm or cancel requests, and reschedule appointments. Administrative corrections to clinical progress require a reason.
- Status, assignment, and schedule changes are recorded in history tables.
- The server checks doctor availability again when creating or rescheduling an appointment.

### Useful API routes

- `GET /api/v1/appointment` — appointments visible to the signed-in role.
- `POST /api/v1/appointment` — admin creates a pending appointment.
- `POST /api/v1/appointment/request` — patient requests an appointment.
- `GET /api/v1/doctor/available?date=YYYY-MM-DD&time=HH:mm:ss` — checks available doctors.
- `PATCH /api/v1/appointment/{id}/status` — submits a workflow transition.
- `POST /api/v1/appointment/{id}/ai-summary-draft` — assigned doctor requests an AI draft.

The AI endpoint accepts clinician notes and returns a draft with `requiresClinicianReview: true`. It does not save a patient record or change appointment status. Notes are sent to the configured AI provider; use only de-identified notes unless the provider is approved for sensitive patient data.

## Configuration

The application reads database settings from environment variables:

| Variable | Purpose |
| --- | --- |
| `DATABASE_URL` | PostgreSQL JDBC URL, for example `jdbc:postgresql://localhost:5432/hospital_db` |
| `DB_USER` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `PORT` | HTTP port; Render provides this for the deployed service |
| `GROQ_API_KEY` | API key for AI draft generation |
| `VITE_API_BASE_URL` | Optional frontend API base URL; defaults to the Render backend URL in production |

Do not commit real passwords or API keys. The local profile is `application-local.properties`; activate it with `SPRING_PROFILES_ACTIVE=local`. Its defaults are intended for local development only.

## Run locally

### Backend

1. Start PostgreSQL and create a database named `hospital_db`.
2. Set `SPRING_PROFILES_ACTIVE=local` and configure `DB_USER` and `DB_PASSWORD` for your local PostgreSQL instance.
3. From `hospital_management`, run:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. Open Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

Run backend tests with:

```powershell
.\mvnw.cmd test
```

### Frontend

From `hospital-frontend`:

```powershell
npm install
npm run dev
```

The development frontend uses `http://localhost:8080` as its API by default. To build and lint:

```powershell
npm run build
npm run lint
```

## Database schema updates

SQL scripts are in `src/main/resources/db/migration`. They are **not applied automatically by Flyway** in this project. For an existing PostgreSQL database, review and apply the applicable scripts in version order before deploying code that depends on their schema changes. Back up the database before applying schema updates.

Hibernate is configured with `ddl-auto=update` for development. This does not replace the SQL migrations or safely handle every existing-schema change.

## Demo accounts

The development seeder creates demo accounts when their usernames do not already exist. These accounts are for local testing only. Replace or remove demo credentials and seeding behavior before using the application with real users.

## Docker note

The current `docker-compose.yml` still defines a MySQL service, while this application is configured with the PostgreSQL JDBC driver. Do not use that Compose setup for the PostgreSQL deployment described above; update the Compose configuration separately if you want a PostgreSQL-based local stack.
