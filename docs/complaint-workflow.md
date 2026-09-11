# Institutional Complaint & Issue Tracking Workflow

The **College Student Feedback Management System** implements a complete, real-world grievance management and issue tracking lifecycle. This document outlines the lifecycle stages, role responsibilities, rule-based prioritization, escalation policies, resolution verification, and zero-PII public portal visibility.

---

## 1. End-to-End Complaint Lifecycle

```
[Student] Lodge Complaint (Category, Title, Description)
                │
                ▼
[System] Rule-based Priority Heuristics (LOW, MEDIUM, HIGH, CRITICAL) + 48h SLA Target Set
                │
                ▼
[System] Public Portal Eligibility Check (HIGH/CRITICAL or SLA Breach → publicVisible = true)
                │
                ▼
[System] Initial IssueUpdate Created ("PENDING") + Student Notification Dispatched
                │
                ▼
[Admin] Grievance Cell Review & Cell Assignment (IT, Facilities, Hostel, Academic, etc.)
                │
                ▼
[System] Status → "IN_PROGRESS" / "ESCALATED" + IssueUpdate Audit Created + Student Notified
                │
                ▼
[Assigned Cell] Resolves Issue & Marks Status → "RESOLVED" (resolvedAt timestamp recorded)
                │
                ▼
[System] IssueUpdate Created + Student Notified of Pending Admin Verification
                │
                ▼
[Admin] Physical / Operational Resolution Verification
                │
                ▼
[Admin] Verifies Resolution & Sets Status → "CLOSED" (verifiedAt timestamp recorded)
                │
                ▼
[System] publicVisible = false (publicRemovedAt recorded) + Issue Removed from Active Public Feed
                │
                ▼
[Database] Complete Issue & Audit Timeline History Permanently Retained in PostgreSQL
```

---

## 2. Categories

Students can submit complaints across standard college functional domains:
- `ACADEMIC` — Academic schedules, lecture pacing, exam conflicts, teaching resources
- `INFRASTRUCTURE` — Classrooms, central AC units, projectors, laboratory hardware
- `HOSTEL` — Accommodation facilities, water purifiers, plumbing, sanitation
- `LIBRARY` — Digital journal proxy, research databases, book availability
- `TRANSPORT` — College bus routes, scheduling, bus passes
- `IT` — Campus Wi-Fi, network switches, lab computing, portal access
- `OTHER` — General institutional grievances

---

## 3. Priority Levels & Backend Heuristic Rules

Priority levels:
- `LOW` — Minor issue affecting a single user; general suggestions or non-urgent fixes.
- `MEDIUM` — Standard operational issues (default SLA: 48 hours).
- `HIGH` — Urgent issue affecting multiple students or critical college services (e.g., power outage, lab Wi-Fi failure, exam conflict).
- `CRITICAL` — Emergency facility failure, physical safety hazards, or major college service collapse requiring immediate intervention.

### Backend Rule-Based Determination
To avoid uncalibrated priorities from the client, the backend analyzes content keywords:
- Keywords like `fire`, `emergency`, `hazard`, `danger`, `medical emergency`, `explosion` automatically elevate the ticket to **CRITICAL**.
- Keywords like `broken`, `power outage`, `water leak`, `no water`, `wifi outage`, `urgent`, `exam conflict`, `server down` automatically elevate the ticket to **HIGH**.
- Keywords like `minor`, `suggestion`, `routine`, `typo` map to **LOW**.

---

## 4. Status Progression & Workflow States

| Status | Description | Permitted Roles |
|---|---|---|
| `PENDING` | Initial state upon lodging; awaiting administrative review and cell routing. | System / Student |
| `IN_PROGRESS` | Ticket assigned to a responsible department/cell and actively being worked on. | Admin |
| `ESCALATED` | Target resolution time (SLA) exceeded or elevated due to high urgency. | Admin / System SLA Job |
| `RESOLVED` | Responsible cell technician/officer has completed physical or operational remediation. Awaiting administrative inspection. | Admin / Assigned Officer |
| `CLOSED` | Final state: Administrator has verified the resolution quality. Ticket is permanently closed and archived. | Admin only |

---

## 5. Administrative Assignment & Audit History

### Assignment
Administrators assign tickets to specialized cells:
- `IT Department`
- `Campus Facilities Cell`
- `Hostel Administration`
- `Library Cell`
- `Academic Office`
- `Transport Cell`
- `Catering Cell`

### IssueUpdate Audit Trail
Every assignment, priority adjustment, and status change generates an immutable `IssueUpdate` record storing:
- `issueType` (`COMPLAINT` / `REQUEST`)
- `oldStatus` & `newStatus`
- `comment` / administrative note
- `updatedBy` (user reference and role)
- `createdAt` timestamp

---

## 6. Student Privacy & Ownership Protection (IDOR Prevention)

1. **Authentication Identification**: Students cannot lodge complaints on behalf of others; the student identity is strictly extracted from the authenticated JWT session (`@AuthenticationPrincipal UserPrincipal`).
2. **Access Control**: Queries to `/api/student/issues/{id}` enforce ownership checks on the backend. If Student A attempts to access Student B's complaint ID, the backend responds with `HTTP 404 Not Found`.

---

## 7. Resolution Verification Rule

- **Rule**: A student cannot close their own complaint.
- **Rule**: Responsible department technician marks the issue as `RESOLVED`.
- **Rule**: Only an authorized **Administrator** can verify the fix and transition `RESOLVED` → `CLOSED`.
- Upon closing, `verifiedAt` and `resolvedAt` are timestamped.

---

## 8. Zero-PII Public Grievance Portal

High-priority unresolved complaints and issues exceeding the 48-hour SLA target are published on the college public portal feed (`GET /api/public/complaints`) to ensure institutional accountability.

### Privacy Safeguards (Zero-PII)
The public endpoint exposes strictly safe fields:
```json
{
  "id": "e7b1a234-...",
  "ticketNumber": "CMP-102",
  "category": "INFRASTRUCTURE",
  "title": "Lab 3 AC & Projector Malfunction",
  "description": "Projector display flickering intermittently during morning lectures in CS Lab 3.",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assignedCell": "Campus Facilities Cell",
  "createdAt": "2026-09-10T14:30:00",
  "publicPublishedAt": "2026-09-10T14:30:00"
}
```

**Never Exposed on Public Feed**:
- Student name
- Student email
- Roll number / Register number
- Phone number
- Internal admin notes
- Specific student identifiers

### Lifecycle on Public Feed
- When a complaint is verified and marked `CLOSED`:
  - `publicVisible` is set to `false`.
  - `publicRemovedAt` is timestamped.
  - The complaint is immediately removed from the active public feed.
  - All database records and audit history remain permanently in PostgreSQL for institutional reporting.
