# College Feedback Management System — Backend

Spring Boot 3.3.x REST API backend for the **College Student Feedback Management System** built with Java 21, Spring Data JPA, Spring Security, JWT authentication, and PostgreSQL.

---

## 🛠 Tech Stack

- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.3.3
- **Data Access**: Spring Data JPA / Hibernate
- **Database**: PostgreSQL (Production) / H2 (Local Development / Testing)
- **Security**: Spring Security 6 + JJWT (0.12.6)
- **Password Hashing**: BCrypt (Strength 10)
- **Validation**: Jakarta Bean Validation
- **Build Tool**: Apache Maven 3.9+

---

## 🏗 Architecture & Package Structure

```
backend/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/college/feedback/
    │   │   ├── CollegeFeedbackApplication.java
    │   │   ├── config/              # CORS, Data Seeder
    │   │   ├── controller/          # REST Controllers (Auth, Student, Faculty, Admin, Notification)
    │   │   ├── dto/                 # Request & Response DTOs
    │   │   ├── entity/              # 11 JPA Entities & Enums
    │   │   ├── exception/           # Global Exception Handler & Custom Errors
    │   │   ├── repository/          # Spring Data Repositories
    │   │   ├── security/            # JWT Utils, Filter, SecurityConfig
    │   │   └── service/             # Business Logic Layer (Auth, Feedback, Complaint, Request, Notification, Analytics)
    │   └── resources/
    │       ├── application.properties
    │       ├── application-postgres.properties
    │       └── application-local.properties
    └── test/                        # 39 Comprehensive Unit & Security Integration Tests
```

---

## ⚙ Environment Variables & Configuration

| Variable | Description | Default |
|---|---|---|
| `PORT` | Server HTTP Port | `8081` |
| `SPRING_PROFILES_ACTIVE` | Active Spring Profile (`local` or `postgres`) | `local` |
| `DB_HOST` | PostgreSQL Hostname | `localhost` |
| `DB_PORT` | PostgreSQL Port | `5432` |
| `DB_NAME` | PostgreSQL Database Name | `college_feedback_db` |
| `DB_USERNAME` | PostgreSQL User | `postgres` |
| `DB_PASSWORD` | PostgreSQL Password | `postgres` |
| `JWT_SECRET` | HMAC-SHA Base64 Secret Key | `404E6352...` |
| `JWT_EXPIRATION_MS` | JWT validity in milliseconds | `86400000` (24h) |
| `CORS_ALLOWED_ORIGINS` | Permitted frontend origins | `http://localhost:8080,http://127.0.0.1:8080,http://localhost:3000,http://localhost:5173,http://127.0.0.1:5500` |

---

## 🚀 How to Run the Backend

### 1. Prerequisites
- **Java 21** installed (`java -version`)
- **Maven 3.8+** installed (`mvn -version` or `./mvnw`)
- *(Optional)* PostgreSQL 14+ running locally on port `5432`

### 2. Build & Test
```bash
cd backend
./mvnw clean test
```

### 3. Package JAR
```bash
./mvnw clean package
```

### 4. Run Application

#### Option A: Run in Local Development Mode (Zero-setup H2 PostgreSQL mode)
```bash
java -jar target/feedback-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=local --server.port=8081
```

#### Option B: Run with PostgreSQL Database
1. Create database in PostgreSQL:
   ```sql
   CREATE DATABASE college_feedback_db;
   ```
2. Run backend with `postgres` profile:
   ```bash
   java -jar target/feedback-backend-1.0.0-SNAPSHOT.jar --spring.profiles.active=postgres --server.port=8081
   ```
   Or set environment variables:
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=college_feedback_db
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_password
   export SPRING_PROFILES_ACTIVE=postgres
   java -jar target/feedback-backend-1.0.0-SNAPSHOT.jar
   ```

---

## 👥 Demo Accounts (Auto-Seeded)

The backend auto-seeds the following demo accounts on initial startup:

| Role | Email | Password | Name | Identifier | Department |
|---|---|---|---|---|---|
| **STUDENT** | `student@example.com` | `Student@123` | Aarav Sharma | `CS-2024-042` | Computer Science |
| **FACULTY** | `faculty@example.com` | `Faculty@123` | Dr. Vikram Malhotra | `FAC-CS-108` | Computer Science |
| **ADMIN** | `admin@example.com` | `Admin@123` | Admin Office | `ADM-001` | Academic Administration |

---

## 🔒 Authentication & Role-Based Access Control (RBAC)

All secured requests must include the JWT token in the `Authorization` header:
```http
Authorization: Bearer <your_jwt_token>
```

### Role-Based Authorization Rules:
- `/api/auth/**` &rarr; Public (Permit All)
- `/api/student/**` &rarr; `ROLE_STUDENT` only
- `/api/faculty/**` &rarr; `ROLE_FACULTY` only
- `/api/admin/**` &rarr; `ROLE_ADMIN` only
- `/api/notifications/**` &rarr; Any Authenticated User

### Security Rules Enforced:
1. **Public Self-Registration Protection**: Public registration is restricted to `STUDENT` accounts. Attempts to register with `ROLE_ADMIN` return `HTTP 400 Bad Request`.
2. **Resource Ownership & IDOR Protection**: Students can only access their own feedback assignments, responses, complaints, requests, and notifications. Student A querying Student B's issue receives `HTTP 404 Not Found`.
3. **Double-Blind Anonymous Feedback**: When `isAnonymous = true`, student identity is strictly masked as `"Anonymous Response"` with `userId = null` and `userIdentifier = null` across all admin views and exports.
4. **CORS Isolation**: CORS is restricted to explicit white-listed development origins without wildcards.
5. **No Password Exposure**: Passwords are saved with BCrypt and annotated with `@JsonIgnore`.

---

## 📡 REST API Catalog

### 1. Authentication Endpoints (`/api/auth`)
- `POST /api/auth/login` — Log in with email & password, returns JWT token & profile.
- `POST /api/auth/register` — Register a new student account (ADMIN registration blocked).
- `GET /api/auth/me` — Get profile for authenticated user session.

### 2. Public Grievance Portal (`/api/public`) — [Public / Zero-PII]
- `GET /api/public/complaints` — Public grievance feed of unresolved high-priority and SLA-overdue tickets (excludes all student PII, name, email, roll number). Automatically removes verified closed tickets.

### 3. Student Endpoints (`/api/student`) — [Role: `STUDENT`]
- `GET /api/student/dashboard` — Overview KPIs (pending feedback, active complaints, requests).
- `GET /api/student/feedback` — List assigned feedback forms.
- `GET /api/student/feedback/{id}` — Form details & question list.
- `POST /api/student/feedback/{id}/response` — Submit evaluation answers and star ratings (supports anonymous mode).
- `GET /api/student/complaints` — Retrieve student's raised complaints.
- `POST /api/student/complaints` — Lodge a new grievance ticket with category, title, description, requested priority.
- `GET /api/student/requests` — Retrieve student's service requests.
- `POST /api/student/requests` — Raise a new service request across categories (DOCUMENT, LIBRARY, ACADEMIC, etc.).
- `GET /api/student/issues/{id}` — Track issue details with resolution timeline & audit trail (ownership verified).
- `GET /api/student/notifications` — Retrieve student notifications.
- `PATCH /api/student/notifications/{id}/read` — Mark notification as read.

### 4. Faculty Endpoints (`/api/faculty`) — [Role: `FACULTY`]
- `GET /api/faculty/dashboard` — Faculty KPIs, response rate, ratings summary.
- `GET /api/faculty/feedback` — Admin-assigned faculty development forms.
- `POST /api/faculty/feedback/{id}/response` — Submit self-evaluation / response.
- `GET /api/faculty/ratings` — Aggregated student ratings breakdown.
- `GET /api/faculty/insights` — AI teaching tips & feedback recommendations.
- `GET /api/faculty/performance` — Syllabus completion & performance scorecard.
- `GET /api/faculty/notifications` — Retrieve faculty notifications.

### 5. Admin Endpoints (`/api/admin`) — [Role: `ADMIN`]
- `GET /api/admin/dashboard` — Executive command center KPIs, response counts, active complaints, pending requests, high-priority alerts.
- `POST /api/admin/feedback` — Create a feedback form with custom questions & audience (DRAFT or PUBLISHED).
- `PUT /api/admin/feedback/{id}` — Update form metadata, deadline, or questions (for forms without responses).
- `DELETE /api/admin/feedback/{id}` — Delete a feedback form (blocked if form has existing responses).
- `POST /api/admin/feedback/{id}/publish` — Validate and publish form, dispatching assignments & notifications.
- `POST /api/admin/feedback/{id}/close` — Close feedback form to terminate new response submissions.
- `POST /api/admin/feedback/{id}/assign` — Assign feedback form to specific users or departments.
- `POST /api/admin/feedback/{id}/questions` — Add a new question to a feedback form.
- `PUT /api/admin/questions/{id}` — Update question text, type, required status, or display order.
- `DELETE /api/admin/questions/{id}` — Delete a question from a form.
- `GET /api/admin/feedback` — List all feedback forms with total responses and average ratings.
- `GET /api/admin/feedback/{id}` — Get single feedback form details and questions.
- `GET /api/admin/responses` — Aggregated and filtered responses (supports `formId`, `category`, `department`, `startDate`, `endDate`).
- `GET /api/admin/feedback/{id}/responses` — View individual responses for a form (strictly enforces anonymity).
- `GET /api/admin/analytics/overview` — System-wide analytics overview, rating distributions, and category breakdowns.
- `GET /api/admin/analytics/feedback/{id}` — Per-campaign analytics, response rates, rating distribution, and question statistics.
- `GET /api/admin/complaints` — View and filter all campus grievances across departments, priority, and status.
- `GET /api/admin/complaints/{id}` — View single complaint with complete audit trail and SLA metadata.
- `PUT /api/admin/complaints/{id}` — Update complaint status, assign department cell, update priority, toggle public visibility, add admin notes.
- `GET /api/admin/requests` — View and filter all service requests.
- `GET /api/admin/requests/{id}` — View single service request with lifecycle details.
- `PUT /api/admin/requests/{id}` — Update request fulfillment status, assign cell, add resolution notes.

### 6. Unified Notification Endpoints (`/api/notifications`) — [Authenticated]
- `GET /api/notifications` — Get notifications for currently logged-in user.
- `GET /api/notifications/unread-count` — Get unread count.
- `PUT /api/notifications/{id}/read` — Mark notification as read.
- `PATCH /api/notifications/{id}/read` — Mark notification as read.

