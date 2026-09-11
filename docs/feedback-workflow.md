# 📋 Feedback Workflow Specification & Architecture Guide
## College Student Feedback Management System — Phase 6

---

## 1. Complete Workflow Lifecycle Overview

The **College Student Feedback Management System** orchestrates a complete, secure, double-blind feedback lifecycle:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          ADMINISTRATOR WORKFLOW                             │
│                                                                             │
│  1. Create Feedback Form ──► 2. Add / Edit Questions ──► 3. Save as Draft  │
│                                                                    │        │
│  4. Publish Form (Validates Questions & Dates) ◄───────────────────┘        │
│        │                                                                    │
│        ▼                                                                    │
│  5. Target Assignment (Assign to specific Students/Faculty or Dept)         │
│        │                                                                    │
│        ▼                                                                    │
│  9. Monitor Live Analytics & Star Distributions                             │
│        │                                                                    │
│        ▼                                                                    │
│ 10. Close Campaign (Status: CLOSED - Prevents Further Submissions)          │
└─────────────────────────────────────┬───────────────────────────────────────┘
                                      │ Assignments Created (PENDING)
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                       RESPONDENT WORKFLOW (Student/Faculty)                 │
│                                                                             │
│  6. View Assigned Forms (GET /api/student/assigned-feedbacks)               │
│        │                                                                    │
│        ▼                                                                    │
│  7. Open Form (POST /api/student/feedback/{id}/start -> IN_PROGRESS)        │
│        │                                                                    │
│        ▼                                                                    │
│  8. Submit Feedback (POST /api/student/feedback/{id}/submit)                │
│     - Required question completeness verification                           │
│     - Rating validation (1 - 5) & Text length limits (<= 2000 chars)        │
│     - Duplicate check (Rejects repeat submissions with 409 Conflict)        │
│     - Double-blind anonymity protection (Nulls user_id if isAnonymous=true) │
│     - Updates assignment status to COMPLETED                                │
│     - Broadcasts admin notification & triggers live analytics recalculation │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Feedback Form Lifecycle States

A feedback form transitions through three distinct lifecycle states:

| Status | Description | Allow Edits / Delete | Allow Submissions | Allow Assignments |
|---|---|---|---|---|
| `DRAFT` | Initial editable state. Admin can configure titles, dates, categories, add/reorder questions. | Yes | No | No |
| `PUBLISHED` | Active survey. Accessible to assigned students/faculty within `startDate` and `endDate`. | Restricted* | Yes (if within active dates) | Yes |
| `CLOSED` | Concluded survey. Read-only for reporting and historical audit. | No | No (returns 400 Form is closed) | No |

\* **Response Protection Rule**: Once a form receives at least one response, structural modifications (adding, updating, or deleting questions) and form deletion are strictly blocked (`400 Bad Request` or `409 Conflict`) to preserve historical data integrity.

---

## 3. Question Engine & Supported Question Types

The system supports 5 flexible question types configured via `QuestionType` enum:

```
┌─────────────────┬───────────────────────────────┬──────────────────────────────────────┐
│ Question Type   │ Data Representation           │ Validation Rules                     │
├─────────────────┼───────────────────────────────┼──────────────────────────────────────┤
│ RATING          │ Integer (1 to 5)              │ Must be between 1 and 5 (inclusive) │
│ STAR_RATING     │ Integer (1 to 5)              │ Must be between 1 and 5 (inclusive) │
│ YES_NO          │ String ("Yes" / "No")         │ Required validation if isRequired    │
│ MULTIPLE_CHOICE │ String (Selected option value)│ Must match options array             │
│ TEXT            │ Text String                   │ Max 2000 characters                  │
└─────────────────┴───────────────────────────────┴──────────────────────────────────────┘
```

### Question Attributes
- `id`: UUID primary key.
- `questionText`: Non-empty prompt text.
- `questionType`: One of `RATING`, `TEXT`, `MULTIPLE_CHOICE`, `YES_NO`, `STAR_RATING`.
- `isRequired`: Boolean flag enforcing response presence upon submission.
- `orderIndex`: Display sequence order in UI rendering.
- `options`: Optional JSON array string for multiple-choice selections.

---

## 4. Double-Blind Privacy & Anonymity Architecture

To encourage honest, unbiased institutional evaluations, the system guarantees **Double-Blind Anonymity**:

```
                       STUDENT SUBMISSION
                    (isAnonymous = true)
                             │
                             ▼
              ┌───────────────────────────────┐
              │      Validation & Guard       │
              │  - Check duplicate submission │
              │  - Mark Assignment: COMPLETED │
              └──────────────┬────────────────┘
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
┌──────────────────────────────┐  ┌──────────────────────────────┐
│  FeedbackAssignment Record   │  │    FeedbackResponse Record   │
│  - user_id: <student_uuid>   │  │  - user_id: NULL             │
│  - form_id: <form_uuid>      │  │  - is_anonymous: TRUE        │
│  - status: COMPLETED         │  │  - answers: [ { ... } ]      │
└──────────────────────────────┘  └──────────────┬───────────────┘
                                                 │
                                                 ▼
                                  ┌──────────────────────────────┐
                                  │   Response DTO (Admin View)  │
                                  │  - userId: null              │
                                  │  - userName: "Anonymous"     │
                                  │  - userEmail: null           │
                                  │  - userIdentifier: null      │
                                  └──────────────────────────────┘
```

1. **Database Level**: When `isAnonymous = true`, `FeedbackResponse.user` is explicitly set to `null` before persistence.
2. **Duplicate Prevention**: The user's `FeedbackAssignment` is marked as `COMPLETED`. Any subsequent submission attempt finds an existing completed assignment and rejects with `HTTP 409 Conflict`.
3. **DTO Layer**: `FeedbackResponseDto` strictly masks student metadata (`userName = "Anonymous Response"`, `userId = null`, `userEmail = null`, `userIdentifier = null`).
4. **No Backdoor Correlation**: The assignment record and response record share no foreign key linkage.

---

## 5. End-to-End REST API Catalog

### 5.1 Admin Endpoints (`/api/admin/**`) — Requires `ROLE_ADMIN`

| Method | Endpoint | Description | Request Body / Params | Response |
|---|---|---|---|---|
| `GET` | `/api/admin/feedback` | List all feedback forms | Optional `status`, `category` | `List<FeedbackFormDto>` |
| `POST` | `/api/admin/feedback` | Create a new form (DRAFT/PUBLISHED) | `FeedbackFormRequest` | `FeedbackFormDto` (201 Created) |
| `GET` | `/api/admin/feedback/{id}` | Get form details with questions | Path variable `id` | `FeedbackFormDto` |
| `PUT` | `/api/admin/feedback/{id}` | Update form metadata | `FeedbackFormRequest` | `FeedbackFormDto` |
| `DELETE` | `/api/admin/feedback/{id}` | Delete form (if no responses) | Path variable `id` | 204 No Content |
| `POST` | `/api/admin/feedback/{id}/publish` | Publish draft form | Path variable `id` | `FeedbackFormDto` |
| `POST` | `/api/admin/feedback/{id}/close` | Close feedback campaign | Path variable `id` | `FeedbackFormDto` |
| `POST` | `/api/admin/feedback/{id}/assign` | Assign form to users/department | `FeedbackAssignmentRequest` | `AssignmentSummaryDto` |
| `POST` | `/api/admin/feedback/{id}/questions` | Add question to form | `QuestionRequest` | `QuestionDto` (201 Created) |
| `PUT` | `/api/admin/questions/{id}` | Update existing question | `QuestionRequest` | `QuestionDto` |
| `DELETE` | `/api/admin/questions/{id}` | Delete question | Path variable `id` | 204 No Content |
| `GET` | `/api/admin/feedback/{id}/analytics` | Live analytics & star distribution | Path variable `id` | `FeedbackAnalyticsDto` |
| `GET` | `/api/admin/responses` | Filtered response audit log | `formId`, `category`, `department`, `startDate`, `endDate` | `List<FeedbackResponseDto>` |

### 5.2 Student Endpoints (`/api/student/**`) — Requires `ROLE_STUDENT`

| Method | Endpoint | Description | Request Body / Params | Response |
|---|---|---|---|---|
| `GET` | `/api/student/assigned-feedbacks` | List assigned forms | None (Authenticated JWT) | `List<FeedbackAssignmentDto>` |
| `GET` | `/api/student/feedback/{id}` | View active feedback form | Path variable `id` | `FeedbackFormDto` |
| `POST` | `/api/student/feedback/{id}/start` | Transition status to `IN_PROGRESS` | Path variable `id` | `FeedbackAssignmentDto` |
| `POST` | `/api/student/feedback/{id}/submit` | Submit answers & complete | `FeedbackResponseRequest` | `FeedbackResponseDto` (201 Created) |
| `GET` | `/api/student/submission-history` | View completed assignments | None (Authenticated JWT) | `List<FeedbackAssignmentDto>` |

### 5.3 Faculty Endpoints (`/api/faculty/**`) — Requires `ROLE_FACULTY`

| Method | Endpoint | Description | Request Body / Params | Response |
|---|---|---|---|---|
| `GET` | `/api/faculty/assigned-feedbacks` | List assigned forms | None (Authenticated JWT) | `List<FeedbackAssignmentDto>` |
| `GET` | `/api/faculty/feedback/{id}` | View feedback form | Path variable `id` | `FeedbackFormDto` |
| `POST` | `/api/faculty/feedback/{id}/start` | Transition to `IN_PROGRESS` | Path variable `id` | `FeedbackAssignmentDto` |
| `POST` | `/api/faculty/feedback/{id}/submit` | Submit answers | `FeedbackResponseRequest` | `FeedbackResponseDto` (201 Created) |

---

## 6. Live Analytics Engine

Every submission dynamically updates institutional metrics:
- **Total Responses**: Real-time counter of submitted responses.
- **Overall Average Rating**: Sum of all `RATING` and `STAR_RATING` answers divided by total ratings.
- **Star Rating Distribution**: Exact counts for 1-Star, 2-Star, 3-Star, 4-Star, and 5-Star ratings.
- **Category-wise Averages**: Aggregated rating scores broken down by category (e.g. *Faculty Performance*, *Course Curriculum*, *Campus Infrastructure*).
- **Per-Question Analytics**: Mean rating score and total answer count for individual survey questions.

---

## 7. Security, Edge Cases & Error Handling

```
┌────────────────────────────────────────┬─────────────┬────────────────────────────────────────────┐
│ Scenario                               │ HTTP Status │ System Action / Error Message              │
├────────────────────────────────────────┼─────────────┼────────────────────────────────────────────┤
│ Missing required question answer       │ 400 Bad Req │ "Answer is required for question: [text]"  │
│ Invalid rating value (<1 or >5)        │ 400 Bad Req │ "Rating must be between 1 and 5"           │
│ Text answer exceeds 2000 chars         │ 400 Bad Req │ "Text answer cannot exceed 2000 chars"     │
│ Publishing form with 0 questions       │ 400 Bad Req │ "Form must have at least one question"     │
│ Publishing form with invalid dates     │ 400 Bad Req │ "End date must be after start date"        │
│ Submitting to CLOSED form              │ 400 Bad Req │ "Feedback form is not accepting responses" │
│ Duplicate submission attempt           │ 409 Conflict│ "You have already submitted this feedback" │
│ Modify/Delete form with responses      │ 400 Bad Req │ "Cannot modify/delete form with responses" │
│ Non-admin deleting feedback form       │ 403 Forbid  │ Access Denied                              │
│ Unauthenticated request                │ 401 Unauth  │ Full authentication required               │
└────────────────────────────────────────┴─────────────┴────────────────────────────────────────────┘
```

---

## 8. Verification & Test Suite Summary

The workflow is validated by **59 passing automated integration tests** covering:
- Complete End-to-End Form Creation -> Publication -> Assignment -> In-Progress -> Submission -> Analytics Lifecycle.
- Double-blind anonymity protection and user data masking.
- Duplicate submission blocking with `409 Conflict`.
- Required question validation and answer length boundaries.
- Response-protection guards preventing deletion or mutation of active forms.
- Role-based authorization restrictions.
