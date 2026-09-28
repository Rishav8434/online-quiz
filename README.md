# Enterprise Online Quiz Application

A production-ready, clean-architecture Online Quiz and Examination Platform built with **Java 21**, **Spring Boot 3**, **Spring Security (stateless JWT)**, **Spring Data JPA / Hibernate**, **MySQL 8**, **Thymeleaf**, and **Bootstrap 5**.

---

## Features

### Role-Based Access
- **Admin (`ROLE_ADMIN`)**:
  - Full quiz lifecycle: Create, Edit, Delete quizzes.
  - Interactive Question & Option builder with live correct-answer toggle.
  - Real-time quiz analytics: total attempts, highest/lowest scores, average percentage.
  - Ranked participant leaderboards.
- **Participant (`ROLE_PARTICIPANT`)**:
  - Browse active quizzes with duration and question counts.
  - Interactive quiz attempt screen with persistent countdown timer, progress bar, and question navigator palette.
  - Automated evaluation upon submission or when the timer expires.
  - Immediate score breakdown and question review (showing participant answer vs. correct answer).
  - Attempt history tracking.

### System & Security
- **Modern Spring Security 6 / Spring Boot 3**:
  - Stateless JWT authentication via `io.jsonwebtoken` (JJWT 0.12.x).
  - Refresh tokens with database persistence and expiration checks.
  - BCrypt password hashing (strength 12).
  - Dual support: Bearer token in `Authorization` header for REST APIs and Cookie synchronization for server-rendered Thymeleaf views.
  - Global Exception Handling with structured JSON error schemas (`ErrorResponse`).
  - OpenAPI 3 / Swagger UI integrated at `/swagger-ui.html`.
  - Asynchronous HTML email dispatch for registration, password reset, and quiz scores.

---

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.3.4, Spring Security, Spring Data JPA, Hibernate, Maven
- **Database**: MySQL 8 (production & docker), H2 (in-memory mode for unit testing)
- **Frontend**: Thymeleaf, Bootstrap 5.3, Bootstrap Icons, JavaScript (Vanilla ES6)
- **Security**: JWT (`jjwt 0.12.6`), BCrypt
- **API Documentation**: SpringDoc OpenAPI 3 (`springdoc-openapi-starter-webmvc-ui 2.6.0`)
- **Testing**: JUnit 5, Mockito, MockMvc, DataJpaTest
- **Containerization**: Docker, Docker Compose

---

## Default Seed Credentials

Upon startup, the system seeds the following accounts (via `DataInitializer`):

| Role | Username | Password | Email |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `Admin@123456` | `admin@onlinequiz.com` |
| **Participant** | `john` | `John@123456` | `john@onlinequiz.com` |

---

## Running Locally

### Prerequisites
- JDK 17 or JDK 21
- Maven 3.8+
- MySQL 8 (optional: local profile defaults to H2 MySQL-compatibility mode if MySQL is absent)

### Step 1: Clone and Navigate
```bash
cd online-quiz
```

### Step 2: Build & Test
```bash
mvn clean test
```

### Step 3: Run Application
```bash
# Standalone run using in-memory mode:
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or with your local MySQL:
mvn spring-boot:run
```

Access the application in your browser:
- Web Application: [http://localhost:8080](http://localhost:8080)
- Swagger / OpenAPI Docs: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI Specification: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Running with Docker & Docker Compose

```bash
# Start MySQL container and Spring Boot application
docker-compose up -d --build

# View container logs
docker-compose logs -f app

# Stop containers and keep data
docker-compose down
```

---

## Database Architecture & Flyway Migrations

The database is version-controlled via Flyway (`src/main/resources/db/migration`):
- `V1__create_roles.sql`: `roles` table (`ROLE_ADMIN`, `ROLE_PARTICIPANT`)
- `V2__create_users.sql`: `users` and `user_roles` with unique constraints & indexes
- `V3__create_refresh_tokens.sql`: `refresh_tokens` with token hashes and expiry timestamps
- `V4__create_password_reset_tokens.sql`: `password_reset_tokens` for secure self-service password recovery
- `V5__create_quizzes.sql`: `quizzes` table with soft delete (`is_deleted`, `deleted_at`), negative marking policies, and pass percentage
- `V6__create_questions.sql`: `questions` table with individual marks and negative mark weights
- `V7__create_options.sql`: `options` table with correct indicator flag
- `V8__create_quiz_attempts.sql`: `quiz_attempts` with server-calculated `start_time`, `end_time`, `score`, and attempt statuses (`IN_PROGRESS`, `SUBMITTED`, `AUTO_SUBMITTED`, `ABANDONED`)
- `V9__create_user_answers.sql`: `user_answers` recording question, option chosen, and marks awarded
- `V10__insert_seed_data.sql`: Base roles and initial records

---

## Architectural Principles & Security Design

1. **Server-Authoritative Timing Engine**:
   - The browser timer is purely visual. When a participant starts a quiz (`POST /api/quizzes/{id}/start`), the server records `startTime` and calculates `expectedEndTime = startTime + duration`.
   - On submission, if the client sends answers past `expectedEndTime` (plus grace period), the backend marks the attempt `AUTO_SUBMITTED`.
2. **Dedicated Scoring Engine (`ScoringService`)**:
   - Formulas:
     $$\text{Raw Score} = \sum \text{Correct Marks} - \sum \text{Negative Marks}$$
     $$\text{Floor applied if policy configured}: \max(0.0, \text{Raw Score})$$
     $$\text{Percentage} = \left(\frac{\text{Score}}{\text{Total Marks}}\right) \times 100$$
3. **Sliding-Window Rate Limiting (`RateLimiterService`)**:
   - Protects `/api/auth/login`, `/api/auth/register`, and `/api/auth/forgot-password` against brute-force attacks and credential stuffing using concurrent sliding-window request queues per client IP.
4. **Soft Delete Architecture**:
   - Quizzes and Questions utilize soft deletes (`is_deleted = true`, `deleted_at = NOW()`).
   - Historical quiz attempts remain consistent and immutable even if an admin deletes or updates questions later.
5. **No N+1 & Batch Fetching**:
   - Queries utilize `@BatchSize(size = 50)` and projection queries to ensure constant query counts when fetching quizzes, questions, and options.

---

## REST API Reference

### Authentication (`/api/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Register new participant account |
| `POST` | `/api/auth/login` | Public | Login with username/email & password; returns JWT & refresh token |
| `POST` | `/api/auth/refresh` | Public | Exchange refresh token for fresh access token |
| `POST` | `/api/auth/logout` | Authenticated | Revoke refresh token |
| `POST` | `/api/auth/forgot-password` | Public | Request password reset token / email |
| `POST` | `/api/auth/reset-password` | Public | Reset password using valid reset token |

### Quiz Management (`/api/quizzes`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/quizzes` | Public | Paginated list of quizzes (supports `search`, `status`, `page`, `size`) |
| `GET` | `/api/quizzes/{id}` | Public | Get quiz details (excludes correct answers for participants) |
| `POST` | `/api/quizzes` | Admin | Create a new quiz |
| `PUT` | `/api/quizzes/{id}` | Admin | Update quiz title, duration, negative marking, pass % |
| `DELETE` | `/api/quizzes/{id}` | Admin | Soft-delete quiz |
| `POST` | `/api/quizzes/{id}/publish` | Admin | Publish quiz (requires at least 1 active question) |
| `POST` | `/api/quizzes/{id}/unpublish` | Admin | Revert quiz to draft status |
| `POST` | `/api/quizzes/{id}/questions` | Admin | Add a new MCQ question (validated for 4 options, 1 correct) |
| `PUT` | `/api/questions/{id}` | Admin | Update question text, marks, options |
| `DELETE` | `/api/questions/{id}` | Admin | Soft-delete question |

### Quiz Attempts & Results
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/quizzes/{id}/start` | Participant | Start quiz attempt (initializes server-side timer) |
| `POST` | `/api/quizzes/{id}/submit` | Participant | Submit answers for scoring |
| `GET` | `/api/attempts/my` | Participant | View paginated personal attempt history |
| `GET` | `/api/results/{attemptId}` | Participant / Admin | Detailed question-by-question result breakdown |
| `GET` | `/api/quizzes/{id}/leaderboard` | Public | Quiz leaderboard ranked by score and duration |

### Admin Analytics & Export (`/api/admin`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/dashboard` | Admin | Aggregate metrics (total users, quizzes, attempts, avg score) |
| `GET` | `/api/admin/analytics` | Admin | Detailed analytics per quiz |
| `GET` | `/api/admin/results` | Admin | Paginated attempt results across all participants |
| `GET` | `/api/admin/quizzes/{id}/export` | Admin | Stream real database records as CSV export |

---

## Environment Variables

| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev`, `prod` |
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/online_quiz_db?useSSL=false&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | MySQL Username | `quiz_user` |
| `DB_PASSWORD` | MySQL Password | `quiz_password` |
| `JWT_SECRET` | Base64-encoded HMAC-SHA secret (>= 256 bits) | (Auto-configured fallback in dev) |
| `JWT_EXPIRATION_MS` | Access token lifespan in milliseconds | `3600000` (1 hour) |
| `REFRESH_TOKEN_EXPIRATION_MS` | Refresh token lifespan | `604800000` (7 days) |
| `MAIL_HOST` | SMTP server host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP server port | `587` |
| `MAIL_USERNAME` | SMTP account username | `example@gmail.com` |
| `MAIL_PASSWORD` | SMTP app password | `app_password` |

---

## Deployment Guides

### Deploy with Docker Compose
```bash
docker compose up -d --build
```
The application will launch on port `8080` and connect to the containerized MySQL 8 database, automatically executing Flyway migrations and seeding initial roles and data.

### Deploy to Render / Railway / Cloud VM
1. Provision a managed MySQL 8 database.
2. Provide the database connection variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`).
3. Set `SPRING_PROFILES_ACTIVE=prod`.
4. Deploy the multi-stage `Dockerfile`.

# online-quiz
