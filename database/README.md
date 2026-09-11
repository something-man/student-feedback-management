# 🗄️ PostgreSQL Database & Data Model Specification
## College Student Feedback Management System

---

## 1. Database Overview

- **Database Engine**: PostgreSQL 15+ (Dual-profile compatibility with file-backed H2 PostgreSQL mode for local dev)
- **Database Name**: `college_feedback_db`
- **Default Port**: `5432`
- **Dialect**: `org.hibernate.dialect.PostgreSQLDialect`
- **DDL Script**: `database/schema.sql`

---

## 2. Relational Table Summary

| Table | Entity | Description | Key Relationships |
|---|---|---|---|
| `users` | `User` | Core system accounts and roles | 1:1 Student/Faculty, 1:N Responses, Complaints, Requests |
| `students` | `Student` | Academic profile for students | 1:1 with `users` (`user_id`) |
| `faculties` | `Faculty` | Departmental & designation profile for faculty | 1:1 with `users` (`user_id`) |
| `feedback_forms` | `FeedbackForm` | Form headers, evaluation criteria & deadlines | 1:N Questions, Assignments, Responses |
| `questions` | `Question` | Questions, types (Star, Text, Yes/No) and order | N:1 with `feedback_forms` |
| `feedback_assignments`| `FeedbackAssignment` | Form assignments per student/faculty | N:1 Form, N:1 User |
| `feedback_responses` | `FeedbackResponse` | Form submissions (Identified or Anonymous) | N:1 Form, N:1 User (Nullable if anonymous) |
| `response_answers` | `ResponseAnswer` | Individual question answers & ratings | N:1 Response, N:1 Question |
| `complaints` | `Complaint` | Student grievance tickets & lifecycle | N:1 Student User, 1:N IssueUpdates |
| `requests` | `Request` | Service/administrative requests | N:1 Student User, 1:N IssueUpdates |
| `issue_updates` | `IssueUpdate` | Timeline audit log for complaints & requests | N:1 Complaint/Request, N:1 UpdatedBy User |
| `notifications` | `Notification` | Real-time user alert dispatch | N:1 User |
| `ai_insights` | `AIInsight` | AI analytics clusters, bottlenecks & alerts | N:1 Form, N:1 Complaint |

---

## 3. Seed & Demo Credentials

| Role | Email | Password | Identifier / Department |
|---|---|---|---|
| **ADMIN** | `admin@example.com` | `Admin@123` | `ADM-001` (Academic Administration) |
| **FACULTY** | `faculty@example.com` | `Faculty@123` | `FAC-CS-108` (Associate Professor, Computer Science) |
| **STUDENT** | `student@example.com` | `Student@123` | `CS-2024-042` (Year 3, Computer Science) |

---

## 4. Key Workflows & Privacy Rules

### A. Feedback Response Anonymity (Double-Blind Privacy)
- When a response is submitted with `isAnonymous = true`:
  - `user_id` is nulled or masked in analytics DTOs.
  - API responses and DTOs return `"Anonymous Response"` for student name, and null for user ID and register number.
  - Privacy is enforced at the JPA Service layer and Jackson serialization layer, preventing client-side data leaks.

### B. Complaint Redressal Lifecycle
$$\text{Student (PENDING)} \longrightarrow \text{Admin (IN\_PROGRESS / Assigned Cell)} \longrightarrow \text{Resolution (RESOLVED)} \longrightarrow \text{Audit (CLOSED)}$$
- Every transition automatically creates an `IssueUpdate` history record and notifies the student.

### C. Public Complaint Portal Support
- `complaints` table includes:
  - `public_visible`: Flags tickets eligible for the public college website.
  - `public_published_at` & `public_removed_at`: Publication lifecycle timestamps.
  - `target_resolution_time`: SLA resolution target (e.g., 48 hours).
  - `resolved_at` & `verified_at`: Resolution and administrative verification timestamps.

---

## 5. Direct Execution via psql

To initialize a fresh PostgreSQL instance manually:
```bash
psql -U postgres -c "CREATE DATABASE college_feedback_db;"
psql -U postgres -d college_feedback_db -f database/schema.sql
```
Or start the Spring Boot backend with profile `postgres`, and Hibernate will validate and auto-manage the schema.
