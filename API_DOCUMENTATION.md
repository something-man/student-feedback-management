# 📡 Centralized API Documentation
## College Feedback Management System (CFMS)

**Base URL**: `http://localhost:8081/api`  
**Authorization**: `Authorization: Bearer <JWT_TOKEN>` (for protected routes)

---

## 1. Authentication Endpoints (`/api/auth`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/login` | Public | Authenticates user; returns JWT token and user profile |
| `POST` | `/api/auth/register` | Public | Self-registration for students/faculty (Role `ADMIN` rejected) |
| `GET` | `/api/auth/me` | Authenticated | Fetches current logged-in user profile |

---

## 2. Public Complaint Portal Endpoints (`/api/public`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/public/complaints` | Public | Returns active high/critical/overdue complaints with Zero PII |

---

## 3. Student Endpoints (`/api/student`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/student/dashboard` | `ROLE_STUDENT` | Fetches student KPI counts, assigned feedback, complaints, requests |
| `GET` | `/api/student/feedback` | `ROLE_STUDENT` | List of feedback forms assigned to the student |
| `GET` | `/api/student/feedback/{id}` | `ROLE_STUDENT` | Feedback form questionnaire and questions |
| `POST` | `/api/student/feedback/{id}/response` | `ROLE_STUDENT` | Submits answers for a feedback form |
| `GET` | `/api/student/complaints` | `ROLE_STUDENT` | List of complaints submitted by the student |
| `POST` | `/api/student/complaints` | `ROLE_STUDENT` | Lodges a new complaint with priority heuristics |
| `GET` | `/api/student/requests` | `ROLE_STUDENT` | List of service requests submitted by student |
| `POST` | `/api/student/requests` | `ROLE_STUDENT` | Submits a new administrative service request |
| `GET` | `/api/student/issues/{id}` | `ROLE_STUDENT` | Detailed issue timeline and audit trail for complaint/request |
| `GET` | `/api/student/notifications` | `ROLE_STUDENT` | Student notification feed |
| `PATCH` | `/api/student/notifications/{id}/read` | `ROLE_STUDENT` | Marks single student notification as read |

---

## 4. Faculty Endpoints (`/api/faculty`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/faculty/dashboard` | `ROLE_FACULTY` | Faculty summary, pending evaluations, teaching ratings |
| `GET` | `/api/faculty/feedback` | `ROLE_FACULTY` | List of feedback forms assigned to faculty |
| `GET` | `/api/faculty/feedback/{id}` | `ROLE_FACULTY` | Questionnaire details for faculty evaluation |
| `POST` | `/api/faculty/feedback/{id}/response` | `ROLE_FACULTY` | Submits faculty self/peer evaluation |
| `GET` | `/api/faculty/ratings` | `ROLE_FACULTY` | Ratings breakdown (Knowledge, Clarity, Doubt Resolution) |
| `GET` | `/api/faculty/insights` | `ROLE_FACULTY` | AI pedagogical improvement recommendations |
| `GET` | `/api/faculty/performance` | `ROLE_FACULTY` | Syllabus milestones and evaluation completion rates |
| `GET` | `/api/faculty/notifications` | `ROLE_FACULTY` | Faculty notification feed |

---

## 5. Admin Command & AI Analytics Endpoints (`/api/admin`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/admin/dashboard` | `ROLE_ADMIN` | Executive KPI counts, alerts, and recent issues |
| `GET` | `/api/admin/feedback` | `ROLE_ADMIN` | List all feedback forms (Draft, Published, Closed) |
| `GET` | `/api/admin/feedback/{id}` | `ROLE_ADMIN` | Single form details with full questions |
| `POST` | `/api/admin/feedback` | `ROLE_ADMIN` | Create new feedback form |
| `PUT` | `/api/admin/feedback/{id}` | `ROLE_ADMIN` | Update feedback form title/deadline/category |
| `DELETE` | `/api/admin/feedback/{id}` | `ROLE_ADMIN` | Delete feedback draft form |
| `POST` | `/api/admin/feedback/{id}/publish` | `ROLE_ADMIN` | Transitions form from Draft to Published |
| `POST` | `/api/admin/feedback/{id}/close` | `ROLE_ADMIN` | Closes campaign to stop further submissions |
| `POST` | `/api/admin/feedback/{id}/assign` | `ROLE_ADMIN` | Dispatches feedback form to target audience/department |
| `GET` | `/api/admin/feedback/{id}/responses` | `ROLE_ADMIN` | Fetches submitted responses with anonymity protection |
| `GET` | `/api/admin/complaints` | `ROLE_ADMIN` | All student complaints with optional filters |
| `GET` | `/api/admin/complaints/{id}` | `ROLE_ADMIN` | Single complaint with timeline updates |
| `PUT` | `/api/admin/complaints/{id}` | `ROLE_ADMIN` | Update status, priority, cell, and resolution notes |
| `GET` | `/api/admin/requests` | `ROLE_ADMIN` | All student requests with optional filters |
| `GET` | `/api/admin/requests/{id}` | `ROLE_ADMIN` | Single request with timeline updates |
| `PUT` | `/api/admin/requests/{id}` | `ROLE_ADMIN` | Update request status (Approved, In Progress, Completed) |
| `GET` | `/api/admin/analytics/overview` | `ROLE_ADMIN` | Global ratings, response rate, category breakdown |
| `GET` | `/api/admin/analytics/trend` | `ROLE_ADMIN` | Multi-month submission volume and rating trends |
| `GET` | `/api/admin/analytics/sentiment` | `ROLE_ADMIN` | NLP Positive %, Neutral %, Negative %, confidence |
| `GET` | `/api/admin/analytics/recurring-issues` | `ROLE_ADMIN` | Semantic cluster groups of repeated complaints |
| `GET` | `/api/admin/analytics/insights` | `ROLE_ADMIN` | Active AI insights and SLA warnings |
| `POST` | `/api/admin/analytics/insights/generate`| `ROLE_ADMIN` | Force refresh of AI intelligence insights |
| `POST` | `/api/admin/reports/generate` | `ROLE_ADMIN` | Generates structured JSON report data for any of 7 types |
| `GET` | `/api/admin/reports/export/pdf` | `ROLE_ADMIN` | Streams OpenPDF generated `.pdf` download |
| `GET` | `/api/admin/reports/export/excel` | `ROLE_ADMIN` | Streams Apache POI generated `.xlsx` download |

---

## 6. Unified Notification Endpoints (`/api/notifications`)

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/notifications` | Authenticated | List current user's notifications |
| `GET` | `/api/notifications/unread-count` | Authenticated | Returns `{ "unreadCount": N }` |
| `PUT` | `/api/notifications/{id}/read` | Authenticated | Marks specific notification as read |
| `PUT` | `/api/notifications/mark-all-read` | Authenticated | Marks all user notifications as read |
