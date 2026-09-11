# Institutional Service Request Workflow

The **College Student Feedback Management System** provides a formal workflow for student service requests, permissions, certificates, and resource access. This document details the lifecycle, supported categories, authorization checks, administrative processing, and timeline tracking.

---

## 1. End-to-End Service Request Lifecycle

```
[Student] Submit Service Request (Category, Title, Details/Justification)
                │
                ▼
[System] Request Number Generated (e.g., REQ-204) + Status → "PENDING"
                │
                ▼
[System] Initial IssueUpdate Created + Student Notified of Submission
                │
                ▼
[Admin] Administrative Review in Central Command Center
                │
                ▼
[Admin] Route & Assign to Responsible Department (Academic Office, Library Cell, etc.)
                │
                ▼
[System] Status → "IN_PROGRESS" + IssueUpdate Audit Created + Student Notified
                │
                ▼
[Admin] Evaluates Eligibility:
        ├── Option A: Request Approved & Processed (Status → "APPROVED")
        ├── Option B: Request Ineligible / Declined (Status → "REJECTED")
        │
        ▼
[Admin] Dispatches Service / Document to Student (Status → "COMPLETED", resolvedAt recorded)
                │
                ▼
[System] IssueUpdate Created + Student Notified of Completion
                │
                ▼
[Student] Views Detailed Lifecycle Timeline & Completion Notes
```

---

## 2. Request Categories

Students can initiate formal administrative requests across six major categories:
- `DOCUMENT` — Bonafide certificates, official transcripts, recommendation letters, medium of instruction certificates
- `LIBRARY` — Digital journal access (IEEE Xplore, ScienceDirect), off-campus proxy credentials, inter-library book loans
- `ACADEMIC` — Elective course changes, audit permissions, lab batch swaps, special attendance considerations
- `FACILITY` — Seminar hall booking, sports equipment reservation, lab computing time extension
- `HOSTEL` — Room maintenance, amenity requests, hostel leave passes
- `TRANSPORT` — Bus route pass issuance, pickup point adjustment
- `GENERAL` — General administrative petitions and student services

---

## 3. Status Progression & Allowed Transitions

| Status | Meaning | Typical Trigger |
|---|---|---|
| `PENDING` | Newly lodged; waiting for initial administrative intake. | Student submission |
| `IN_PROGRESS` | Assigned to a specific department officer and currently being processed. | Admin routing |
| `APPROVED` | Request requirements verified and cleared for fulfillment. | Admin approval |
| `REJECTED` | Request declined due to policy or missing prerequisite requirements. | Admin rejection with reason |
| `COMPLETED` | Service fulfilled (document generated, credentials issued, access granted). | Admin completion |

---

## 4. Student Tracking & Dynamic Audit Trail

Students can track their request in real-time from the **Student Dashboard**:
- **Tracker Modal**: Displays ticket number, submission timestamp, current state badge, and assigned department.
- **Dynamic Timeline**: Populated directly from immutable `IssueUpdate` database records:
  1. *Submitted* — Logged by student upon initial creation.
  2. *Under Review / In Progress* — Logged by administrative officer with routing notes.
  3. *Approved / Rejected* — Logged with decision criteria.
  4. *Completed* — Logged with delivery / dispatch details.

---

## 5. Security & Ownership Enforcement

- **Role Guard**: Only users with `ROLE_STUDENT` can invoke `/api/student/requests`.
- **Identity Isolation**: Student identity is bound to the verified JWT; a student cannot submit requests under another student ID.
- **IDOR Protection**: The `/api/student/issues/{id}` endpoint verifies that `request.student.id` matches the authenticated `UserPrincipal.id`. Unauthorized access attempts result in `HTTP 404 Not Found`.
- **Administrative Privileges**: Only `ROLE_ADMIN` users can approve, reject, reassign, or mark requests as completed (`/api/admin/requests/{id}`).

---

## 6. Targeted Notifications

Targeted notifications are dispatched to the student's notification center:
- Submission confirmation: *"Your service request REQ-xxx has been submitted successfully."*
- Assignment update: *"Your request REQ-xxx has been assigned to Academic Office."*
- Decision update: *"Your request REQ-xxx has been approved."*
- Completion update: *"Your request REQ-xxx has been completed."*
- Rejection update: *"Your request REQ-xxx has been rejected."*
