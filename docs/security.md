# 🛡️ Security Architecture & Role-Based Authorization Specification
## College Student Feedback Management System

---

## 1. Security Architecture Overview

The **College Student Feedback Management System** implements defense-in-depth security principles across authentication, authorization, resource ownership, cryptographic data handling, and privacy preservation:

```
┌──────────────────────────────────────────────────────────────┐
│                    Client Browser Layer                      │
│   (AuthStore + Bearer Header Interceptor + Role Route Guard) │
└──────────────────────────────┬───────────────────────────────┘
                               │ HTTP / HTTPS + Authorization: Bearer <JWT>
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                 Spring Security Filter Chain                 │
│  - CorsFilter (Strict Origin White-listing)                  │
│  - JwtAuthenticationFilter (Signature & Expiration Check)    │
│  - SecurityContextHolder (Populate Authenticated Principal)   │
└──────────────────────────────┬───────────────────────────────┘
                               │
            ┌──────────────────┼──────────────────┐
            ▼                  ▼                  ▼
     [/api/student/**]  [/api/faculty/**]  [/api/admin/**]
       ROLE_STUDENT       ROLE_FACULTY       ROLE_ADMIN
            │                  │                  │
            └──────────────────┼──────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────┐
│                  Service Layer Authorization                 │
│  - IDOR Protection: studentId == currentUser.getId()         │
│  - Anonymity Filter: Double-Blind Privacy Enforcement        │
│  - Privilege Escalation Guard: Reject ADMIN Registration     │
└──────────────────────────────┬───────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────┐
│             PostgreSQL / JPA Persistence Layer               │
│  - BCrypt Hashed Passwords (Salted, Strength 10)             │
│  - Foreign Key Constraints & PrePersist Timestamps           │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. Authentication & JWT Specifications

### A. JWT Structure & Claims
- **Algorithm**: HMAC-SHA384 / HMAC-SHA256 with Base64-encoded 256-bit secret key.
- **Subject (`sub`)**: User email address (e.g. `student@example.com`).
- **Claims**:
  - `userId`: Canonical UUID string of the authenticated user.
  - `role`: Role string (`STUDENT`, `FACULTY`, `ADMIN`).
  - `iat`: Issued-at epoch timestamp.
  - `exp`: Expiration epoch timestamp (Default: 24 hours / 86,400,000 ms).

### B. Verification Flow
1. Client submits credentials via `POST /api/auth/login`.
2. `DaoAuthenticationProvider` verifies credentials against `BCryptPasswordEncoder`.
3. Server returns signed JWT token along with sanitized user profile.
4. Client stores JWT in `AuthStore` (`localStorage` / `sessionStorage`).
5. Axios/Fetch interceptor injects `Authorization: Bearer <token>` on all subsequent requests.
6. `JwtAuthenticationFilter` validates token signature and loads `UserPrincipal` into Spring's `SecurityContext`.

### C. Session & Expiration Handling
- **401 Unauthorized**: Interceptor automatically purges stored tokens, displays session expired notification, and redirects to `login.html`.
- **Logout**: Clears `AuthStore` tokens and resets application state.

---

## 3. Role-Based Access Control (RBAC) Matrix

| Endpoint / Resource | Public | `ROLE_STUDENT` | `ROLE_FACULTY` | `ROLE_ADMIN` |
|---|:---:|:---:|:---:|:---:|
| `POST /api/auth/login` | ✅ | ✅ | ✅ | ✅ |
| `POST /api/auth/register` (Student) | ✅ | ✅ | ❌ | ❌ |
| `POST /api/auth/register` (Admin) | ❌ **(400 Blocked)** | ❌ | ❌ | ❌ |
| `GET /api/student/dashboard` | ❌ | ✅ | ❌ **(403)** | ❌ **(403)** |
| `GET /api/student/feedback` | ❌ | ✅ | ❌ **(403)** | ❌ **(403)** |
| `POST /api/student/feedback/{id}/response` | ❌ | ✅ | ❌ **(403)** | ❌ **(403)** |
| `GET /api/student/complaints` | ❌ | ✅ | ❌ **(403)** | ❌ **(403)** |
| `POST /api/student/complaints` | ❌ | ✅ | ❌ **(403)** | ❌ **(403)** |
| `GET /api/student/requests` | ❌ | ✅ | ❌ **(403)** | ❌ **(403)** |
| `POST /api/student/requests` | ❌ | ✅ | ❌ **(403)** | ❌ **(403)** |
| `GET /api/student/issues/{id}` | ❌ | ✅ **(Owner Only)** | ❌ **(403)** | ❌ **(403)** |
| `GET /api/faculty/dashboard` | ❌ | ❌ **(403)** | ✅ | ❌ **(403)** |
| `GET /api/faculty/ratings` | ❌ | ❌ **(403)** | ✅ | ❌ **(403)** |
| `GET /api/faculty/insights` | ❌ | ❌ **(403)** | ✅ | ❌ **(403)** |
| `GET /api/admin/dashboard` | ❌ | ❌ **(403)** | ❌ **(403)** | ✅ |
| `POST /api/admin/feedback` | ❌ | ❌ **(403)** | ❌ **(403)** | ✅ |
| `PUT /api/admin/complaints/{id}` | ❌ | ❌ **(403)** | ❌ **(403)** | ✅ |
| `PUT /api/admin/requests/{id}` | ❌ | ❌ **(403)** | ❌ **(403)** | ✅ |
| `GET /api/notifications` | ❌ | ✅ **(Owner Only)** | ✅ **(Owner Only)** | ✅ **(Owner Only)** |

---

## 4. Insecure Direct Object Reference (IDOR) & Resource Ownership Defense

### Vulnerability Context:
In multi-tenant or multi-user systems, a malicious user may tamper with ticket IDs (e.g. `GET /api/student/issues/{uuid}`) to view other students' private grievances.

### Defense Mechanism:
1. Controllers **never trust** `studentId` or `userId` supplied in the request body.
2. The user identity is extracted strictly from `@AuthenticationPrincipal UserPrincipal currentUser`.
3. In `ComplaintService` & `RequestService`, queries are constrained:
   ```java
   public ComplaintDto getStudentComplaintById(UUID complaintId, UUID studentId) {
       Complaint complaint = complaintRepository.findById(complaintId)
               .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));
       if (!complaint.getStudent().getId().equals(studentId)) {
           throw new ResourceNotFoundException("Complaint not found with id: " + complaintId);
       }
       return ComplaintDto.fromEntity(complaint);
   }
   ```
4. Attempts by Student A to inspect Student B's complaint result in `HTTP 404 Not Found`.

---

## 5. Double-Blind Anonymous Feedback Privacy Enforcement

### Privacy Guarantee:
When a student selects **"Submit anonymously"** on an assigned evaluation questionnaire:
1. **Database Decoupling**: In `FeedbackService`, if `isAnonymous == true`, the foreign key `user_id` on `FeedbackResponse` is set to `NULL`.
2. **DTO Identity Masking**: In `FeedbackResponseDto.fromEntity()`, the user fields are explicitly sanitized:
   ```java
   if (Boolean.TRUE.equals(r.getIsAnonymous()) || r.getUser() == null) {
       dto.setUserId(null);
       dto.setUserName("Anonymous Response");
       dto.setUserIdentifier(null);
   }
   ```
3. **Admin Exemption Policy**: Even administrators querying `/api/admin/feedback/{id}/responses` cannot uncover the submitter's name, email, or student roll number.

---

## 6. Password Security & Cryptography

1. **Hashing Algorithm**: `BCryptPasswordEncoder` with strength factor 10.
2. **Salt Generation**: Automatically generated per-user random 128-bit salt embedded into the hash string.
3. **JSON Filtering**: Passwords in entity models are marked `@JsonIgnore` and never included in DTOs.

---

## 7. CORS & Network Defense

1. **Origin White-list**: Explicitly restricted to known frontend origins (`http://localhost:8080`, `http://127.0.0.1:8080`, `http://localhost:5173`, `http://127.0.0.1:5500`).
2. **No Wildcard with Credentials**: Wildcard origin patterns (`*`) are disallowed when `allowCredentials` is enabled.
3. **Frame Options**: Configured to deny clickjacking.

---

## 8. Automated Security Test Results

All 39 backend tests pass with **0 Failures, 0 Errors**:

| Test Class | Tests Run | Result | Coverage |
|---|:---:|:---:|---|
| `SecurityIntegrationTest` | 9 | ✅ PASSED | Login, Bad Password, Role Auth, 403 Forbidden, 401 Unauthorized, IDOR Defense, Anonymous Masking, Admin Registration Blocking |
| `AuthControllerTest` | 6 | ✅ PASSED | Login validation, registration, duplicate emails |
| `StudentControllerTest` | 6 | ✅ PASSED | Dashboard, feedback, complaints, requests, unauthorized attempts |
| `FacultyControllerTest` | 4 | ✅ PASSED | Dashboard, ratings, performance, insights |
| `AdminControllerTest` | 5 | ✅ PASSED | Dashboard, form creation, complaints, requests |
| `DatabaseIntegrationTest` | 8 | ✅ PASSED | JPA entities, relationships, cascades, indexes |
| `CollegeFeedbackApplicationTests` | 1 | ✅ PASSED | Spring Context bootstrapping & data seeder |
