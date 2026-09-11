-- ============================================================================
-- College Student Feedback Management System
-- PostgreSQL Database Schema Definition (DDL)
-- Database: college_feedback_db
-- ============================================================================

-- Create Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================================
-- 1. USERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('STUDENT', 'FACULTY', 'ADMIN')),
    identifier VARCHAR(50),
    department VARCHAR(100),
    year_of_study INT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_role ON users(role);

-- ============================================================================
-- 2. STUDENTS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS students (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    register_number VARCHAR(50) NOT NULL UNIQUE,
    batch VARCHAR(50),
    course VARCHAR(100),
    department VARCHAR(100),
    semester INT
);

CREATE INDEX IF NOT EXISTS idx_student_reg_no ON students(register_number);

-- ============================================================================
-- 3. FACULTIES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS faculties (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(100),
    designation VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_faculty_emp_id ON faculties(employee_id);

-- ============================================================================
-- 4. FEEDBACK FORMS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS feedback_forms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED' CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED')),
    target_audience VARCHAR(20) NOT NULL DEFAULT 'STUDENTS' CHECK (target_audience IN ('STUDENTS', 'FACULTY', 'BOTH')),
    target_department VARCHAR(100),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    allow_anonymous BOOLEAN NOT NULL DEFAULT TRUE,
    created_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_form_status ON feedback_forms(status);
CREATE INDEX IF NOT EXISTS idx_form_category ON feedback_forms(category);

-- ============================================================================
-- 5. QUESTIONS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    form_id UUID NOT NULL REFERENCES feedback_forms(id) ON DELETE CASCADE,
    question_text VARCHAR(500) NOT NULL,
    question_type VARCHAR(30) NOT NULL DEFAULT 'RATING' CHECK (question_type IN ('RATING', 'TEXT', 'MULTIPLE_CHOICE', 'YES_NO', 'STAR_RATING', 'CRITERIA_SELECT')),
    required BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_question_form ON questions(form_id);

-- ============================================================================
-- 6. FEEDBACK ASSIGNMENTS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS feedback_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    form_id UUID NOT NULL REFERENCES feedback_forms(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    assigned_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'EXPIRED')),
    assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deadline TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT uq_form_user UNIQUE (form_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_assignment_user ON feedback_assignments(user_id);
CREATE INDEX IF NOT EXISTS idx_assignment_status ON feedback_assignments(status);

-- ============================================================================
-- 7. FEEDBACK RESPONSES TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS feedback_responses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    form_id UUID NOT NULL REFERENCES feedback_forms(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED' CHECK (status IN ('DRAFT', 'SUBMITTED')),
    overall_rating DOUBLE PRECISION,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_response_form ON feedback_responses(form_id);
CREATE INDEX IF NOT EXISTS idx_response_status ON feedback_responses(status);

-- ============================================================================
-- 8. RESPONSE ANSWERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS response_answers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    response_id UUID NOT NULL REFERENCES feedback_responses(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    rating INT,
    answer VARCHAR(2000)
);

CREATE INDEX IF NOT EXISTS idx_answer_response ON response_answers(response_id);
CREATE INDEX IF NOT EXISTS idx_answer_question ON response_answers(question_id);

-- ============================================================================
-- 9. COMPLAINTS TABLE (Includes Public Portal Support)
-- ============================================================================
CREATE TABLE IF NOT EXISTS complaints (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_number VARCHAR(30) NOT NULL UNIQUE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED', 'CLOSED')),
    assigned_to_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    assigned_cell VARCHAR(100),
    admin_note VARCHAR(1000),
    public_visible BOOLEAN NOT NULL DEFAULT FALSE,
    public_published_at TIMESTAMP,
    public_removed_at TIMESTAMP,
    target_resolution_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    verified_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_complaint_ticket ON complaints(ticket_number);
CREATE INDEX IF NOT EXISTS idx_complaint_status ON complaints(status);
CREATE INDEX IF NOT EXISTS idx_complaint_priority ON complaints(priority);
CREATE INDEX IF NOT EXISTS idx_complaint_student ON complaints(student_id);

-- ============================================================================
-- 10. REQUESTS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    request_number VARCHAR(30) NOT NULL UNIQUE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'COMPLETED', 'RESOLVED')),
    assigned_to_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    admin_note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_request_number ON requests(request_number);
CREATE INDEX IF NOT EXISTS idx_request_status ON requests(status);
CREATE INDEX IF NOT EXISTS idx_request_student ON requests(student_id);

-- ============================================================================
-- 11. ISSUE UPDATES (Status History & Audit Trail)
-- ============================================================================
CREATE TABLE IF NOT EXISTS issue_updates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    issue_type VARCHAR(20) NOT NULL CHECK (issue_type IN ('COMPLAINT', 'REQUEST')),
    issue_id UUID,
    complaint_id UUID REFERENCES complaints(id) ON DELETE CASCADE,
    request_id UUID REFERENCES requests(id) ON DELETE CASCADE,
    updated_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    comment VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_issue_update_complaint ON issue_updates(complaint_id);
CREATE INDEX IF NOT EXISTS idx_issue_update_request ON issue_updates(request_id);

-- ============================================================================
-- 12. NOTIFICATIONS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    type VARCHAR(50),
    link VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notification_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notification_read ON notifications(is_read);

-- ============================================================================
-- 13. AI INSIGHTS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS ai_insights (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_type VARCHAR(50),
    source_id UUID,
    insight_type VARCHAR(30) NOT NULL CHECK (insight_type IN ('SENTIMENT', 'RECURRING_ISSUE', 'PRIORITY', 'RECOMMENDATION', 'SUMMARY', 'CATEGORY', 'TREND', 'BOTTLENECK', 'SLA_ALERT')),
    title VARCHAR(200) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    priority VARCHAR(20),
    confidence DOUBLE PRECISION,
    form_id UUID REFERENCES feedback_forms(id) ON DELETE CASCADE,
    complaint_id UUID REFERENCES complaints(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_insight_type ON ai_insights(insight_type);
CREATE INDEX IF NOT EXISTS idx_insight_source ON ai_insights(source_type, source_id);
