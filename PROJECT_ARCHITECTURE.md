# 🏛️ System Architecture Documentation
## College Feedback Management System (CFMS)

---

## 1. High-Level Architecture Overview

The **College Feedback Management System** is engineered as a decoupled, multi-tier enterprise web application designed for high security, strict role-based access control (RBAC), double-blind anonymity, and real-time AI-assisted decision intelligence.

```
+-------------------------------------------------------------------------+
|                              CLIENT LAYER                               |
|   Vanilla HTML5 + CSS3 Design Tokens + Vanilla ES6+ JS + Chart.js 4.4   |
|   (Landing Page, Login Portal, Student, Faculty & Admin Dashboards)     |
+-------------------------------------------------------------------------+
                                    |
                         REST / JSON / Bearer JWT
                                    |
+-------------------------------------------------------------------------+
|                           API GATEWAY / FILTER                          |
|         CorsFilter -> JwtAuthenticationFilter -> SecurityFilterChain    |
+-------------------------------------------------------------------------+
                                    |
+-------------------------------------------------------------------------+
|                            CONTROLLER LAYER                             |
|  AuthController | StudentController | FacultyController | AdminController|
|             PublicController | NotificationController                   |
+-------------------------------------------------------------------------+
                                    |
+-------------------------------------------------------------------------+
|                             SERVICE LAYER                               |
|   AuthService | FeedbackService | ComplaintService | RequestService     |
|   NotificationService | AIService | AnalyticsService | ReportService     |
+-------------------------------------------------------------------------+
                                    |
+-------------------------------------------------------------------------+
|                           DATA ACCESS LAYER                             |
|                Spring Data JPA Repositories + Hibernate                 |
+-------------------------------------------------------------------------+
                                    |
+-------------------------------------------------------------------------+
|                            DATABASE LAYER                               |
|            PostgreSQL 16 (Production) / H2 PostgreSQL-Mode (Local)      |
+-------------------------------------------------------------------------+
```

---

## 2. Core Subsystems

### 2.1 Authentication & Security Subsystem
- **Stateless Bearer JWT Tokens**: Signed via HMAC-SHA256 with 24-hour validity.
- **BCrypt Hashing**: Password entropy secured with work factor 10.
- **RBAC Matrix**: Enforced at URL matcher level and method-level (`@PreAuthorize("hasRole('ROLE')")`).
- **Anonymity Layer**: Double-blind masking where student references are decoupled from quantitative rating analytics and text summaries when `is_anonymous = true`.

### 2.2 Feedback Lifecycle Engine
1. **Form Creation**: Dynamic question builder supporting Rating (1–5 Stars), Text, Yes/No, and Multiple Choice.
2. **Draft & Publish**: Forms can be saved in `DRAFT` or activated in `PUBLISHED` status.
3. **Audience Dispatch**: Bulk target assignment to `STUDENTS`, `FACULTY`, or `BOTH`, with department-level filtering.
4. **Submission Validation**: Single-submission enforcement prevents duplicate submissions.
5. **Closure**: `CLOSED` status locks further submissions and freezes historical analytics.

### 2.3 Grievance & Service Request Lifecycle Engine
- **Unique Ticket Identifiers**: Auto-generated `CMP-XXX` and `REQ-XXX` codes.
- **Dynamic SLA Timers**: Default 48-hour resolution clock.
- **Audit Logging**: Every status transition (`PENDING` -> `IN_PROGRESS` -> `ESCALATED` -> `RESOLVED` -> `CLOSED`) creates immutable `IssueUpdate` timeline records with author metadata.
- **Resolution Verification**: Direct student or administrative verification required to move resolved issues to `CLOSED`.

### 2.4 AI Intelligence & NLP Pipeline
- **Lexical Sentiment Analyzer**: Analyzes textual remarks and returns Positive %, Neutral %, Negative %, confidence score, and key sentiment drivers.
- **Semantic Complaint Clustering**: Aggregates similar grievances into actionable operational clusters (Hostel Wi-Fi, Water filtration, Classroom AV, Transport).
- **Algorithmic Priority Calculator**: Computes priority score (0–100) using repetition, sentiment polarity, and SLA aging.
- **Automated Recommendations**: Generates executive recommendations based on lowest category scores and highest ticket volumes.

### 2.5 Multi-Format Reporting Engine
- **7 Core Report Profiles**: Overall Feedback, Faculty Performance, Course Feedback, Infrastructure Audit, Complaint Lifecycle, Request Fulfillment, and AI Intelligence.
- **OpenPDF**: High-resolution vector-rendered PDF exports with KPI cards, color-coded tables, and recommendations.
- **Apache POI**: Styled multi-column `.xlsx` spreadsheets.

---

## 3. Deployment Topology

The application supports containerized orchestration via Docker Compose:
- **`cfms_postgres`**: PostgreSQL 16 Alpine container with healthcheck probes.
- **`cfms_backend`**: Spring Boot container running OpenJDK 21 Alpine on port 8081.
- **`cfms_frontend`**: Nginx Alpine container serving static assets on port 8080 and reverse-proxying `/api/` traffic.
