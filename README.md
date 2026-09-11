# 🎓 College Feedback Management System (CFMS)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Tests](https://img.shields.io/badge/Tests-68%2F68%20Passing-success.svg)](TESTING.md)

An enterprise-grade, privacy-first **College Feedback Management System** built with Spring Boot 3.3.4 (Java 21), PostgreSQL 16, a zero-dependency vanilla JS / CSS3 responsive web frontend, token-based AI NLP analytics, multi-format institutional report generator, and double-blind anonymity guarantees.

---

## 🚀 Quick Start (Docker Compose)

The fastest way to spin up the entire application (PostgreSQL, Backend API, and Web UI):

```bash
# 1. Clone repository & configure environment
cp .env.example .env

# 2. Build and launch all services in detached mode
docker compose up --build -d
```

- **Frontend & Public Grievances Portal**: [http://localhost:8080](http://localhost:8080)
- **Backend REST API**: [http://localhost:8081/api](http://localhost:8081/api)
- **Health Check**: [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

---

## 🔑 Demo Credentials

On first startup, the system automatically initializes with pre-configured demonstration accounts:

| Role | Username | Password | Purpose |
|:---|:---|:---|:---|
| 👨‍💼 **Administrator** | `admin` | `admin123` | Institutional management, report generation, form builder, AI analytics |
| 👨‍🏫 **Faculty Member** | `faculty1` | `faculty123` | Department faculty view, ratings dashboard, pedagogical AI feedback |
| 👨‍🎓 **Student** | `student1` | `student123` | Assigned feedback evaluation, raising complaints, tracking tickets |

---

## 📱 Live Pages & Interfaces

| Page | File | Description |
|:---|:---|:---|
| **Landing Page** | [`index.html`](index.html) | Brand navbar, hero metrics, How It Works workflow, 6 platform feature cards, and **Public Grievance Resolution Feed** (Zero-PII). |
| **Login Page** | [`login.html`](login.html) | Centered card, 3 interactive selectable role tabs (STUDENT, FACULTY, ADMIN), password reveal toggle, and dynamic role-based routing. |
| **Student Dashboard** | [`student-dashboard.html`](student-dashboard.html) | Student voice console with 4 summary metric cards, pending feedback evaluation cards with star-rating modal, complaint submission & tracking, and notifications. *(No Create Feedback per strict RBAC)* |
| **Faculty Dashboard** | [`faculty-dashboard.html`](faculty-dashboard.html) | Teaching evaluation console with 4 summary cards, Admin-assigned feedback cards, ⭐ 4.4 / 5 ratings breakdown, and AI pedagogical improvement suggestions. *(No Complaints/Requests in sidebar per strict RBAC)* |
| **Admin Dashboard** | [`admin-dashboard.html`](admin-dashboard.html) | Central administration hub with campus metric cards, 4 horizontal progress bars, interactive Form Builder modal, ticket resolution center, **AI Analytics Engine**, and **7-in-1 Report Generator** (PDF / Excel / Print). |

---

## ⚡ Key Features

1. **Double-Blind Anonymity**: Evaluative feedback submissions are completely decoupled from student user IDs to ensure candid, retaliation-free academic reviews.
2. **AI Intelligence Engine**:
   - Token-based Sentiment Analysis (Positive, Neutral, Negative, Mixed polarity with negation handling).
   - Dynamic Keyword Extraction & Recurring Issue Clustering.
   - Multi-factor Urgency / Priority Scoring ($0 - 100$).
   - Dynamic Executive Summaries and categorized institutional recommendations.
3. **Institutional Report Generator**:
   - 7 Report Types: Overall Institutional, Faculty Evaluation, Course Performance, Infrastructure & Facilities, Student Complaints, Student Requests, and AI Insights.
   - **OpenPDF** vector-table document exports.
   - **Apache POI** multi-tab formatted Excel (`.xlsx`) workbooks.
   - CSS-optimized browser print views.
4. **Public Grievance Resolution Portal**:
   - Transparent public feed displaying anonymized campus issue statuses.
   - Zero-PII sanitization (student identities, emails, and room numbers stripped).
   - Anonymous complaint tracking by cryptographic ticket token (e.g., `TRK-1001`).
   - SLA tracking with automatic resolution archiving.
5. **Unified Notifications Subsystem**:
   - Cross-role event notifications (feedback assigned, feedback submitted, complaint status transitions, admin broadcasts).
   - Real-time unread counter badge, topbar notification modal, individual mark-read, and mark-all-read capabilities.
6. **Robust Role-Based Access Control (RBAC)**:
   - Spring Security Filter Chain with method-level `@PreAuthorize`.
   - HMAC-SHA256 stateless JWT authentication and 12-round BCrypt password hashing.

---

## 📚 Complete Documentation Suite

All system documentation is organized in the repository root:

- 📋 [**Final Project Audit (`FINAL_PROJECT_AUDIT.md`)**](FINAL_PROJECT_AUDIT.md) — Exhaustive system audit, endpoint inventory, database state, and feature verification.
- 🏗️ [**Architecture Guide (`PROJECT_ARCHITECTURE.md`)**](PROJECT_ARCHITECTURE.md) — System architecture, layered design, component diagram, data flow, and design patterns.
- 🗄️ [**Database Schema (`DATABASE_SCHEMA.md`)**](DATABASE_SCHEMA.md) — Complete ERD, PostgreSQL DDL table schemas, indexes, and double-blind isolation logic.
- 🌐 [**REST API Documentation (`API_DOCUMENTATION.md`)**](API_DOCUMENTATION.md) — Complete REST API reference with HTTP methods, auth headers, payloads, and response examples.
- 🔒 [**Security Architecture (`SECURITY.md`)**](SECURITY.md) — Threat model, BCrypt, JWT auth, RBAC, double-blind anonymity, and OWASP Top 10 mitigations.
- 🧠 [**AI Engine Guide (`AI_DOCUMENTATION.md`)**](AI_DOCUMENTATION.md) — NLP sentiment analysis, clustering heuristics, priority formula, and LLM adapter interfaces.
- 🧪 [**Testing Guide (`TESTING.md`)**](TESTING.md) — Automated Maven test suites (68/68 passing), unit/integration coverage, and 21-step manual verification plan.
- 🚀 [**Deployment Manual (`DEPLOYMENT.md`)**](DEPLOYMENT.md) — Docker Compose instructions, local standalone setup, Nginx SSL reverse proxy, and backup procedures.
- 🏆 [**Final Status Report (`FINAL_STATUS.md`)**](FINAL_STATUS.md) — Executive summary of deliverables, verification matrix, and production certification.

---

## 🛠️ Local Development (Without Docker)

### Backend (Spring Boot + Java 21)
```powershell
# Set Java 21 environment
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# Run tests
cd backend
.\mvnw.cmd test

# Run backend application (Local embedded mode)
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

### Frontend (Static Web Server)
```powershell
# In project root directory
python -m http.server 8080
```
Open [http://localhost:8080](http://localhost:8080) in your web browser.

---

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
