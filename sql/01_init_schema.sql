-- ==============================================================================
-- Schema Initialization for F-School System (PostgreSQL 18)
-- Courses: PRM393, SWR302 | Class: SE1945
-- Conforms to modern PostgreSQL best practices:
-- - BIGINT GENERATED ALWAYS AS IDENTITY for PKs
-- - Explicit foreign key indexing
-- - Proper CHECK constraints, NOT NULL constraints, TIMESTAMPTZ
-- ==============================================================================

-- 1. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    roll_number TEXT UNIQUE, -- e.g. 'SE194501' for students or 'GV001' for lecturers
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    phone_number TEXT,
    avatar_url TEXT,
    role TEXT NOT NULL DEFAULT 'STUDENT' CHECK (role IN ('STUDENT', 'LECTURER', 'PARENT', 'ADMIN')),
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),
    token_version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_lower_email ON users (LOWER(email));
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_roll_number ON users (roll_number);

-- 2. PARENT_STUDENTS RELATION TABLE
CREATE TABLE IF NOT EXISTS parent_students (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    relationship_type TEXT NOT NULL DEFAULT 'PARENT' CHECK (relationship_type IN ('FATHER', 'MOTHER', 'GUARDIAN', 'PARENT')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_parent_student UNIQUE (parent_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_parent_students_parent_id ON parent_students (parent_id);
CREATE INDEX IF NOT EXISTS idx_parent_students_student_id ON parent_students (student_id);

-- 3. COURSES TABLE
CREATE TABLE IF NOT EXISTS courses (
    course_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_code TEXT NOT NULL UNIQUE, -- e.g. 'PRM393', 'SWR302'
    course_name TEXT NOT NULL,
    credits INT NOT NULL DEFAULT 3 CHECK (credits > 0),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_courses_code ON courses (course_code);

-- 4. CLASSES TABLE
CREATE TABLE IF NOT EXISTS classes (
    class_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_code TEXT NOT NULL, -- e.g. 'SE1945'
    course_id BIGINT NOT NULL REFERENCES courses(course_id) ON DELETE RESTRICT,
    lecturer_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    semester TEXT NOT NULL, -- e.g. 'Fall2026'
    academic_year INT NOT NULL DEFAULT 2026 CHECK (academic_year >= 2000),
    room_default TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_class_course_semester UNIQUE (class_code, course_id, semester)
);

CREATE INDEX IF NOT EXISTS idx_classes_course_id ON classes (course_id);
CREATE INDEX IF NOT EXISTS idx_classes_lecturer_id ON classes (lecturer_id);

-- 5. CLASS_ENROLLMENTS TABLE
CREATE TABLE IF NOT EXISTS class_enrollments (
    enrollment_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(class_id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'ENROLLED' CHECK (status IN ('ENROLLED', 'DROPPED', 'COMPLETED')),
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_class_student UNIQUE (class_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_class_enrollments_class_id ON class_enrollments (class_id);
CREATE INDEX IF NOT EXISTS idx_class_enrollments_student_id ON class_enrollments (student_id);

-- 6. SCHEDULES TABLE (schedules - no meet_url, no lesson_topic, with session_no)
CREATE TABLE IF NOT EXISTS schedules (
    schedule_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(class_id) ON DELETE CASCADE,
    slot_number INT NOT NULL CHECK (slot_number BETWEEN 1 AND 8),
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room TEXT NOT NULL,
    session_no INT NOT NULL DEFAULT 1 CHECK (session_no > 0), -- SessionNo: 1, 2, 11
    lecturer_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    note TEXT,
    status TEXT NOT NULL DEFAULT 'UPCOMING' CHECK (status IN ('UPCOMING', 'PRESENT', 'ABSENT', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_schedules_class_id ON schedules (class_id);
CREATE INDEX IF NOT EXISTS idx_schedules_lecturer_id ON schedules (lecturer_id);
CREATE INDEX IF NOT EXISTS idx_schedules_date ON schedules (date);
CREATE INDEX IF NOT EXISTS idx_schedules_class_date ON schedules (class_id, date);

-- 7. GRADES TABLE
CREATE TABLE IF NOT EXISTS grades (
    grade_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES classes(class_id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    assessment_name TEXT NOT NULL, -- e.g. 'Lab 1', 'Progress Test 1', 'Practical Exam', 'Final Exam'
    weight NUMERIC(5,2) NOT NULL CHECK (weight >= 0 AND weight <= 100),
    score NUMERIC(4,2) CHECK (score >= 0 AND score <= 10),
    feedback TEXT,
    status TEXT NOT NULL DEFAULT 'PUBLISHED' CHECK (status IN ('DRAFT', 'PUBLISHED')),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_student_class_assessment UNIQUE (student_id, class_id, assessment_name)
);

CREATE INDEX IF NOT EXISTS idx_grades_class_id ON grades (class_id);
CREATE INDEX IF NOT EXISTS idx_grades_student_id ON grades (student_id);

-- 8. REQUESTS TABLE (Đơn từ)
CREATE TABLE IF NOT EXISTS requests (
    request_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    requester_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE, -- Student or Parent
    request_type TEXT NOT NULL CHECK (request_type IN ('ATTENDANCE_EXEMPTION', 'COURSE_WITHDRAWAL', 'LEAVE_OF_ABSENCE', 'GRADE_REVIEW', 'CERTIFICATE_REQUEST', 'OTHER')),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    attachment_url TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'APPROVED', 'REJECTED')),
    handler_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    response_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_requests_student_id ON requests (student_id);
CREATE INDEX IF NOT EXISTS idx_requests_requester_id ON requests (requester_id);
CREATE INDEX IF NOT EXISTS idx_requests_status ON requests (status);

-- 9. CLUBS TABLE
CREATE TABLE IF NOT EXISTS clubs (
    club_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    club_name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN ('ACADEMIC', 'ARTS', 'SPORTS', 'VOLUNTEER', 'TECHNOLOGY', 'MEDIA', 'OTHER')),
    avatar_url TEXT,
    banner_url TEXT,
    leader_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    contact_email TEXT,
    member_count INT NOT NULL DEFAULT 1 CHECK (member_count >= 0),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_clubs_leader_id ON clubs (leader_id);
CREATE INDEX IF NOT EXISTS idx_clubs_category ON clubs (category);

-- 10. CLUB_MEMBERSHIPS TABLE
CREATE TABLE IF NOT EXISTS club_memberships (
    membership_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    club_id BIGINT NOT NULL REFERENCES clubs(club_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role TEXT NOT NULL DEFAULT 'MEMBER' CHECK (role IN ('LEADER', 'VICE_LEADER', 'CORE_MEMBER', 'MEMBER')),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_club_user UNIQUE (club_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_club_memberships_club_id ON club_memberships (club_id);
CREATE INDEX IF NOT EXISTS idx_club_memberships_user_id ON club_memberships (user_id);

-- 11. NEWS TABLE
CREATE TABLE IF NOT EXISTS news (
    news_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title TEXT NOT NULL,
    summary TEXT NOT NULL,
    content TEXT NOT NULL,
    thumbnail_url TEXT,
    author_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    category TEXT NOT NULL DEFAULT 'GENERAL' CHECK (category IN ('GENERAL', 'ACADEMIC', 'EVENT', 'ACTIVITY', 'SCHOLARSHIP')),
    views_count INT NOT NULL DEFAULT 0 CHECK (views_count >= 0),
    is_published BOOLEAN NOT NULL DEFAULT true,
    published_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_news_author_id ON news (author_id);
CREATE INDEX IF NOT EXISTS idx_news_category ON news (category);
CREATE INDEX IF NOT EXISTS idx_news_published_at ON news (published_at DESC);

-- 12. NOTIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE, -- NULL for Broadcast notifications
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    type TEXT NOT NULL CHECK (type IN ('NEWS', 'SECURITY', 'ACADEMIC', 'REQUEST', 'SYSTEM')),
    target_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_created ON notifications (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications (user_id, is_read);

-- 13. NOTIFICATION_READS TABLE
CREATE TABLE IF NOT EXISTS notification_reads (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    notification_id BIGINT NOT NULL REFERENCES notifications(notification_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    read_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_notification_user_read UNIQUE (notification_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_notification_reads_user_id ON notification_reads (user_id);
