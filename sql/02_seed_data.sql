-- ==============================================================================
-- Seed Data for F-School System (PostgreSQL 18)
-- Includes realistic test accounts for Students, Lecturers, Parents, and Admin
-- ==============================================================================

-- 1. USERS SEED
INSERT INTO users (roll_number, email, password_hash, full_name, phone_number, avatar_url, role, status)
VALUES
    -- Admin
    ('AD001', 'admin@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'System Administrator', '0901234567', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 'ADMIN', 'ACTIVE'),
    -- Lecturers
    ('GV001', 'anhnv@fe.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Nguyen Van Anh', '0912345678', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lecturer1', 'LECTURER', 'ACTIVE'),
    ('GV002', 'dungpt@fe.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Phan Thanh Dung', '0912111222', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lecturer2', 'LECTURER', 'ACTIVE'),
    ('GV003', 'huyentt@fe.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Tran Thi Thu Huyen', '0912222333', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lecturer3', 'LECTURER', 'ACTIVE'),
    ('GV004', 'anhpn@fe.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Pham Nhat Anh', '0912333444', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lecturer4', 'LECTURER', 'ACTIVE'),
    ('GV005', 'thangld@fe.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Le Duc Thang', '0912444555', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lecturer5', 'LECTURER', 'ACTIVE'),
    -- Students
    ('HE190784', 'vananvu429@gmail.com', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Vũ Văn An', '0912000111', 'https://api.dicebear.com/7.x/avataaars/svg?seed=HE190784', 'STUDENT', 'ACTIVE'),
    ('SE194501', 'bttse194501@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Tran Thi Bao', '0987654321', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student1', 'STUDENT', 'ACTIVE'),
    ('SE194502', 'cle194502@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Le Van Cuong', '0978123456', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student2', 'STUDENT', 'ACTIVE'),
    ('SE194503', 'dtmse194503@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Do Thi Mai', '0981112233', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student3', 'STUDENT', 'ACTIVE'),
    ('SE194504', 'hoangnm194504@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Nguyen Minh Hoang', '0982223344', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student4', 'STUDENT', 'ACTIVE'),
    ('SE194505', 'phuongnt194505@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Ngo Thu Phuong', '0983334455', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student5', 'STUDENT', 'ACTIVE'),
    ('SE194506', 'minhnv194506@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Nguyen Van Minh', '0984445566', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student6', 'STUDENT', 'ACTIVE'),
    ('SE194507', 'trungdq194507@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Dinh Quang Trung', '0985556677', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student7', 'STUDENT', 'ACTIVE'),
    ('SE194508', 'hahtt194508@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Hoang Thi Thu Ha', '0986667788', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student8', 'STUDENT', 'ACTIVE'),
    -- Parents
    ('PH001', 'phu_huynh_bao@gmail.com', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Tran Van Dung', '0933112233', 'https://api.dicebear.com/7.x/avataaars/svg?seed=parent1', 'PARENT', 'ACTIVE'),
    ('PH002', 'phu_huynh_cuong@gmail.com', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Pham Thi Hoa', '0944556677', 'https://api.dicebear.com/7.x/avataaars/svg?seed=parent2', 'PARENT', 'ACTIVE'),
    ('PH003', 'phu_huynh_mai@gmail.com', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Do Van Hai', '0935551122', 'https://api.dicebear.com/7.x/avataaars/svg?seed=parent3', 'PARENT', 'ACTIVE'),
    ('PH004', 'phu_huynh_hoang@gmail.com', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Nguyen Thi Lan', '0936662233', 'https://api.dicebear.com/7.x/avataaars/svg?seed=parent4', 'PARENT', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

-- 2. PARENT_STUDENTS SEED
INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'FATHER'
FROM users p, users s
WHERE p.email = 'phu_huynh_bao@gmail.com' AND s.email = 'bttse194501@fpt.edu.vn'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'MOTHER'
FROM users p, users s
WHERE p.email = 'phu_huynh_cuong@gmail.com' AND s.email = 'cle194502@fpt.edu.vn'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'FATHER'
FROM users p, users s
WHERE p.email = 'phu_huynh_mai@gmail.com' AND s.email = 'dtmse194503@fpt.edu.vn'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'GUARDIAN'
FROM users p, users s
WHERE p.email = 'phu_huynh_mai@gmail.com' AND s.email = 'hoangnm194504@fpt.edu.vn'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'MOTHER'
FROM users p, users s
WHERE p.email = 'phu_huynh_hoang@gmail.com' AND s.email = 'hoangnm194504@fpt.edu.vn'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'GUARDIAN'
FROM users p, users s
WHERE p.email = 'phu_huynh_hoang@gmail.com' AND s.email = 'phuongnt194505@fpt.edu.vn'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'GUARDIAN'
FROM users p, users s
WHERE p.email = 'phu_huynh_bao@gmail.com' AND s.email = 'vananvu429@gmail.com'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'PARENT'
FROM users p, users s
WHERE p.email = 'phu_huynh_cuong@gmail.com' AND s.email = 'vananvu429@gmail.com'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'PARENT'
FROM users p, users s
WHERE p.email = 'phu_huynh_bao@gmail.com' AND s.email = 'minhnv194506@fpt.edu.vn'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'PARENT'
FROM users p, users s
WHERE p.email = 'phu_huynh_cuong@gmail.com' AND s.email = 'trungdq194507@fpt.edu.vn'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'GUARDIAN'
FROM users p, users s
WHERE p.email = 'phu_huynh_mai@gmail.com' AND s.email = 'hahtt194508@fpt.edu.vn'
ON CONFLICT (parent_id, student_id) DO NOTHING;

INSERT INTO parent_students (parent_id, student_id, relationship_type)
SELECT p.user_id, s.user_id, 'GUARDIAN'
FROM users p, users s
WHERE p.email = 'phu_huynh_hoang@gmail.com' AND s.email = 'cle194502@fpt.edu.vn'
ON CONFLICT (parent_id, student_id) DO NOTHING;

-- 3. COURSES SEED
INSERT INTO courses (course_code, course_name, credits, description)
VALUES
    ('PRM393', 'Mobile Programming (Android/Flutter)', 3, 'Lap trinh ung dung di dong nang cao tren nen tang Flutter va Android Native.'),
    ('SWR302', 'Software Requirement', 3, 'Khao sat, thu thap va dac ta yeu cau phan mem theo quy trinh chuan.'),
    ('SWP391', 'Software Development Project', 3, 'Du an phat trien phan mem thuc te theo mo hinh Scrum.'),
    ('MLN122', 'Marxist-Leninist Philosophy', 3, 'Triet hoc Mac-Lenin va tu tuong co ban.'),
    ('JIT401', 'Japanese IT 1', 3, 'Tieng Nhat chuyen nganh Cong nghe Thong tin co ban.'),
    ('PRJ301', 'Java Web Application Development', 3, 'Phat trien ung dung web tren nen tang Java Servlet va JSP.'),
    ('PRN211', 'Basic Cross-Platform with .NET', 3, 'Lap trinh ung dung da nen tang voi C# va .NET Core.'),
    ('MAS291', 'Statistics & Probability', 3, 'Xac suat thong ke ung dung cho nganh Khoa hoc May tinh.'),
    ('SWE201', 'Software Engineering Introduction', 3, 'Tong quan ve quy trinh phan mem va chu trinh phat trien SDLC.'),
    ('DBI202', 'Introduction to Databases', 3, 'Co so du lieu quan he va ngon ngu truy van SQL tieu chuan.'),
    ('CSD201', 'Data Structures and Algorithms', 3, 'Cau truc du lieu va giai thuat nang cao bang Java.'),
    ('IOT102', 'Internet of Things Fundamentals', 3, 'Kien thuc co ban ve he thong nhung va Internet of Things.'),
    ('ITE302c', 'Ethics in IT', 3, 'Dao duc nghe nghiep va an toan thong tin trong nganh CNTT.'),
    ('MAD101', 'Discrete Mathematics', 3, 'Toan roi rac cho nganh Cong nghe Thong tin.')
ON CONFLICT (course_code) DO NOTHING;

-- 4. CLASSES SEED
INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'SE1945', c.course_id, u.user_id, 'Fall2026', 2026, 'BE-301'
FROM courses c, users u
WHERE c.course_code = 'PRM393' AND u.email = 'anhnv@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'SE1945', c.course_id, u.user_id, 'Fall2026', 2026, 'BE-305'
FROM courses c, users u
WHERE c.course_code = 'SWR302' AND u.email = 'anhnv@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'SE1945', c.course_id, u.user_id, 'Fall2026', 2026, 'BE-302'
FROM courses c, users u
WHERE c.course_code = 'SWP391' AND u.email = 'anhnv@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'SE1945', c.course_id, u.user_id, 'Fall2026', 2026, 'AL-R201'
FROM courses c, users u
WHERE c.course_code = 'MLN122' AND u.email = 'dungpt@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'SE1945', c.course_id, u.user_id, 'Fall2026', 2026, 'BE-211'
FROM courses c, users u
WHERE c.course_code = 'PRJ301' AND u.email = 'huyentt@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'SE1946', c.course_id, u.user_id, 'Fall2026', 2026, 'BE-303'
FROM courses c, users u
WHERE c.course_code = 'PRM393' AND u.email = 'anhpn@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'SE1946', c.course_id, u.user_id, 'Fall2026', 2026, 'BE-304'
FROM courses c, users u
WHERE c.course_code = 'SWR302' AND u.email = 'thangld@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'SE1946', c.course_id, u.user_id, 'Fall2026', 2026, 'AL-R305'
FROM courses c, users u
WHERE c.course_code = 'DBI202' AND u.email = 'dungpt@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'MKT1918-DIG', c.course_id, u.user_id, 'Summer2026', 2026, 'AL-R101'
FROM courses c, users u
WHERE c.course_code = 'MLN122' AND u.email = 'dungpt@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'SE1945-JS(IT)', c.course_id, u.user_id, 'Fall2026', 2026, 'AL-R102'
FROM courses c, users u
WHERE c.course_code = 'JIT401' AND u.email = 'huyentt@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'IA1901', c.course_id, u.user_id, 'Fall2026', 2026, 'BE-101'
FROM courses c, users u
WHERE c.course_code = 'ITE302c' AND u.email = 'thangld@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'DS1902', c.course_id, u.user_id, 'Summer2026', 2026, 'AL-R202'
FROM courses c, users u
WHERE c.course_code = 'MAS291' AND u.email = 'anhpn@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

INSERT INTO classes (class_code, course_id, lecturer_id, semester, academic_year, room_default)
SELECT 'AI1903', c.course_id, u.user_id, 'Summer2026', 2026, 'BE-401'
FROM courses c, users u
WHERE c.course_code = 'CSD201' AND u.email = 'anhnv@fe.edu.vn'
ON CONFLICT (class_code, course_id, semester) DO NOTHING;

-- 5. CLASS_ENROLLMENTS SEED
INSERT INTO class_enrollments (class_id, student_id, status)
SELECT cl.class_id, s.user_id, 'ENROLLED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email IN ('vananvu429@gmail.com', 'bttse194501@fpt.edu.vn', 'cle194502@fpt.edu.vn')
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (class_id, student_id) DO NOTHING;

INSERT INTO class_enrollments (class_id, student_id, status)
SELECT cl.class_id, s.user_id, 'ENROLLED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email IN ('vananvu429@gmail.com', 'bttse194501@fpt.edu.vn', 'cle194502@fpt.edu.vn')
WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWR302'
ON CONFLICT (class_id, student_id) DO NOTHING;

INSERT INTO class_enrollments (class_id, student_id, status)
SELECT cl.class_id, s.user_id, 'ENROLLED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email IN ('dtmse194503@fpt.edu.vn', 'hoangnm194504@fpt.edu.vn', 'phuongnt194505@fpt.edu.vn')
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (class_id, student_id) DO NOTHING;

INSERT INTO class_enrollments (class_id, student_id, status)
SELECT cl.class_id, s.user_id, 'ENROLLED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email IN ('dtmse194503@fpt.edu.vn', 'hoangnm194504@fpt.edu.vn', 'phuongnt194505@fpt.edu.vn')
WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWR302'
ON CONFLICT (class_id, student_id) DO NOTHING;

INSERT INTO class_enrollments (class_id, student_id, status)
SELECT cl.class_id, s.user_id, 'ENROLLED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email IN ('minhnv194506@fpt.edu.vn', 'trungdq194507@fpt.edu.vn')
WHERE cl.class_code = 'SE1946' AND c.course_code = 'PRM393'
ON CONFLICT (class_id, student_id) DO NOTHING;

INSERT INTO class_enrollments (class_id, student_id, status)
SELECT cl.class_id, s.user_id, 'ENROLLED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email IN ('hahtt194508@fpt.edu.vn')
WHERE cl.class_code = 'SE1946' AND c.course_code = 'SWR302'
ON CONFLICT (class_id, student_id) DO NOTHING;

INSERT INTO class_enrollments (class_id, student_id, status)
SELECT cl.class_id, s.user_id, 'ENROLLED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email IN ('vananvu429@gmail.com', 'bttse194501@fpt.edu.vn', 'cle194502@fpt.edu.vn')
WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWP391'
ON CONFLICT (class_id, student_id) DO NOTHING;

-- 6. SCHEDULES SEED (No meet_url, with session_no)
INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 2, '2026-10-12', '10:00:00', '12:20:00', 'AL-R402', 2, cl.lecturer_id, 'Session 2: MLN122 Overview', 'ABSENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 1, '2026-10-13', '07:30:00', '09:50:00', 'BE-211', 11, cl.lecturer_id, 'Session 11: Flutter UI Testing', 'PRESENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 1, '2026-10-14', '07:30:00', '09:50:00', 'BE-301', 12, cl.lecturer_id, 'Session 12: Flutter Architecture & Database', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 3, '2026-10-16', '10:00:00', '12:20:00', 'BE-305', 1, cl.lecturer_id, 'Session 1: Introduction to Requirements Engineering', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWR302'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

-- Schedules for week 19/10/2026 - 25/10/2026 (Matching active week)
INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 2, '2026-10-19', '10:00:00', '12:20:00', 'AL-R402', 4, cl.lecturer_id, 'Session 4: Mobile Programming Overview', 'PRESENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 1, '2026-10-20', '07:30:00', '09:50:00', 'BE-211', 13, cl.lecturer_id, 'Session 13: Software Requirements Verification', 'PRESENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWR302'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 1, '2026-10-21', '07:30:00', '09:50:00', 'AL-R402', 5, cl.lecturer_id, 'Session 5: Flutter State Management & Clean Arch', 'ABSENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 3, '2026-10-23', '10:00:00', '12:20:00', 'BE-305', 2, cl.lecturer_id, 'Session 2: Requirements Analysis & Use Cases', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWR302'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

-- Additional schedules for expanded classes and subjects
INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 2, '2026-10-20', '10:00:00', '12:20:00', 'BE-302', 1, cl.lecturer_id, 'Session 1: Agile & Scrum Kickoff', 'PRESENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWP391'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 2, '2026-10-22', '10:00:00', '12:20:00', 'BE-302', 2, cl.lecturer_id, 'Session 2: Backlog Grooming & Sprint Planning', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWP391'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 4, '2026-10-19', '12:50:00', '15:10:00', 'AL-R201', 3, cl.lecturer_id, 'Session 3: Materialist Dialectics', 'PRESENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'MLN122'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 4, '2026-10-21', '12:50:00', '15:10:00', 'AL-R201', 4, cl.lecturer_id, 'Session 4: Economic Concepts in Philosophy', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'MLN122'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 3, '2026-10-20', '10:00:00', '12:20:00', 'BE-211', 5, cl.lecturer_id, 'Session 5: Servlet Lifecycle & Filters', 'PRESENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRJ301'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 3, '2026-10-24', '10:00:00', '12:20:00', 'BE-211', 6, cl.lecturer_id, 'Session 6: MVC with JSP and JSTL', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRJ301'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 1, '2026-10-20', '07:30:00', '09:50:00', 'BE-303', 1, cl.lecturer_id, 'Session 1: Flutter Setup & Dart Refresher', 'PRESENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1946' AND c.course_code = 'PRM393'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 1, '2026-10-22', '07:30:00', '09:50:00', 'BE-303', 2, cl.lecturer_id, 'Session 2: Widgets & Layout Principles', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1946' AND c.course_code = 'PRM393'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 5, '2026-10-19', '15:20:00', '17:40:00', 'BE-304', 1, cl.lecturer_id, 'Session 1: Software Requirements Fundamentals', 'PRESENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1946' AND c.course_code = 'SWR302'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 5, '2026-10-23', '15:20:00', '17:40:00', 'BE-304', 2, cl.lecturer_id, 'Session 2: Requirements Elicitation Techniques', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1946' AND c.course_code = 'SWR302'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 2, '2026-10-24', '10:00:00', '12:20:00', 'AL-R102', 1, cl.lecturer_id, 'Session 1: Japanese Alphabet & IT Terminology', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945-JS(IT)' AND c.course_code = 'JIT401'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 1, '2026-10-24', '07:30:00', '09:50:00', 'BE-101', 1, cl.lecturer_id, 'Session 1: Professional IT Ethics', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'IA1901' AND c.course_code = 'ITE302c'
ON CONFLICT (class_id, date, slot_number) DO NOTHING;

-- 7. GRADES SEED
INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Lab 1', 10.00, 9.50, 'Thuc hien giao dien dep, dung tieu chuan Flutter.', 'PUBLISHED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email = 'bttse194501@fpt.edu.vn'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Progress Test 1', 15.00, 8.50, 'Code logic tot, can luu y xu ly bat dong bo.', 'PUBLISHED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email = 'bttse194501@fpt.edu.vn'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Practical Exam', 25.00, 9.00, 'Hoan thanh xuat sac bai thi thuc hanh.', 'PUBLISHED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email = 'bttse194501@fpt.edu.vn'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Lab 1', 10.00, 8.00, 'Dat yeu cau co ban.', 'PUBLISHED'
FROM classes cl
JOIN courses c ON cl.course_id = c.course_id
JOIN users s ON s.email = 'cle194502@fpt.edu.vn'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Lab 1', 10.00, 9.00, 'Xuat sac, UI dep va clean code.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'vananvu429@gmail.com'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Progress Test 1', 15.00, 9.50, 'Thuc hien tot cac cau hoi ly thuyet va thuc hanh.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'vananvu429@gmail.com'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Practical Exam', 25.00, 9.80, 'Diem tuyet doi cho phan Clean Architecture.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'vananvu429@gmail.com'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Assignment 1', 20.00, 8.50, 'Dac ta SRS day du va ro rang.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'bttse194501@fpt.edu.vn'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWR302'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Assignment 1', 20.00, 8.00, 'Bo sung them phan non-functional requirements.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'cle194502@fpt.edu.vn'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWR302'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Lab 1', 10.00, 8.50, 'Giao dien hop ly, xu ly duoc widget co ban.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'dtmse194503@fpt.edu.vn'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Progress Test 1', 15.00, 8.00, 'Bai kiem tra hoan thanh dung han.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'dtmse194503@fpt.edu.vn'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Lab 1', 10.00, 7.50, 'Can chu y format code theo dung chuan Flutter.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'hoangnm194504@fpt.edu.vn'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Lab 1', 10.00, 9.00, 'Giao dien sang tao, xu ly responsive tot.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'phuongnt194505@fpt.edu.vn'
WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Lab 1', 10.00, 8.00, 'Lam tot phan layout va routing.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'minhnv194506@fpt.edu.vn'
WHERE cl.class_code = 'SE1946' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Lab 1', 10.00, 8.50, 'Su dung tot Provider va State Management.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'trungdq194507@fpt.edu.vn'
WHERE cl.class_code = 'SE1946' AND c.course_code = 'PRM393'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

INSERT INTO grades (class_id, student_id, assessment_name, weight, score, feedback, status)
SELECT cl.class_id, s.user_id, 'Assignment 1', 20.00, 9.00, 'Viet use case rat chi tiet va chinh xac.', 'PUBLISHED'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id JOIN users s ON s.email = 'hahtt194508@fpt.edu.vn'
WHERE cl.class_code = 'SE1946' AND c.course_code = 'SWR302'
ON CONFLICT (student_id, class_id, assessment_name) DO NOTHING;

-- 8. REQUESTS SEED (Đơn từ)
INSERT INTO requests (student_id, requester_id, request_type, title, content, status, response_note)
SELECT s.user_id, p.user_id, 'LEAVE_OF_ABSENCE', 'Don xin phep nghi hoc vi ly do suc khoe', 'Kinh gui thay chu nhiem, toi la phu huynh em Bao xin phep cho em nghi buoi hoc ngay mai.', 'APPROVED', 'Da ghi nhan don, chuc em mau khoe.'
FROM users s, users p
WHERE s.email = 'bttse194501@fpt.edu.vn' AND p.email = 'phu_huynh_bao@gmail.com';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status)
SELECT s.user_id, s.user_id, 'GRADE_REVIEW', 'Don phuc khao diem Progress Test 1', 'Em kinh de nghi xem lai cau hoi so 3 trong bai kiem tra.', 'PENDING'
FROM users s
WHERE s.email = 'cle194502@fpt.edu.vn';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status, handler_id, response_note)
SELECT s.user_id, s.user_id, 'CERTIFICATE_REQUEST', 'Xin giay xac nhan sinh vien tam hoan nghia vu quan su', 'Kinh gui phong CTSV, em can giay xac nhan sinh vien de bo sung ho so.', 'APPROVED', a.user_id, 'Da cap giay tai ban so 3 phong CTSV.'
FROM users s, users a
WHERE s.email = 'vananvu429@gmail.com' AND a.email = 'admin@fpt.edu.vn';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status, handler_id, response_note)
SELECT s.user_id, p.user_id, 'LEAVE_OF_ABSENCE', 'Xin phep nghi hoc buoi thuc hanh vi ly do gia dinh', 'Gia dinh co viec dot xuat nen xin phep cho em An nghi buoi hoc ngay mai.', 'APPROVED', l.user_id, 'Da chap thuan nghi phep co phep.'
FROM users s, users p, users l
WHERE s.email = 'vananvu429@gmail.com' AND p.email = 'phu_huynh_bao@gmail.com' AND l.email = 'anhnv@fe.edu.vn';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status)
SELECT s.user_id, s.user_id, 'ATTENDANCE_EXEMPTION', 'Don xin mien diem danh tiet hoc the chat', 'Em bi chan thuong co chan nen xin phep mien tap luyen thuc hanh.', 'PROCESSING'
FROM users s
WHERE s.email = 'dtmse194503@fpt.edu.vn';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status)
SELECT s.user_id, s.user_id, 'GRADE_REVIEW', 'Xin phuc khao bai thi giua ky', 'Em kinh de nghi xem lai cau 4 do he thong cham cham diem.', 'PENDING'
FROM users s
WHERE s.email = 'hoangnm194504@fpt.edu.vn';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status, handler_id, response_note)
SELECT s.user_id, p.user_id, 'COURSE_WITHDRAWAL', 'Don xin rut mon hoc ky Fall 2026', 'Do trung lich cong tac dot xuat, gia dinh xin rut mon JIT401.', 'REJECTED', a.user_id, 'Da qua han chot rut mon theo quy dinh nha truong.'
FROM users s, users p, users a
WHERE s.email = 'phuongnt194505@fpt.edu.vn' AND p.email = 'phu_huynh_hoang@gmail.com' AND a.email = 'admin@fpt.edu.vn';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status, handler_id, response_note)
SELECT s.user_id, s.user_id, 'CERTIFICATE_REQUEST', 'Xin cap bang diem tam thoi', 'Em xin cap bang diem tam thoi de lam ho so thuc tap doanh nghiep.', 'APPROVED', a.user_id, 'Ban co the tai bang diem dien tu tren cong DVC.'
FROM users s, users a
WHERE s.email = 'minhnv194506@fpt.edu.vn' AND a.email = 'admin@fpt.edu.vn';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status, handler_id, response_note)
SELECT s.user_id, s.user_id, 'LEAVE_OF_ABSENCE', 'Don xin nghi om buoi chieu ngay 21/10', 'Em bi sot cao can di kham bac si.', 'APPROVED', l.user_id, 'Da xac nhan don, chuc em mau binh phuc.'
FROM users s, users l
WHERE s.email = 'trungdq194507@fpt.edu.vn' AND l.email = 'dungpt@fe.edu.vn';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status)
SELECT s.user_id, s.user_id, 'OTHER', 'Don de nghi cap lai the sinh vien bi mat', 'Em bi danh roi the sinh vien tai khu vuc thu vien.', 'PENDING'
FROM users s
WHERE s.email = 'hahtt194508@fpt.edu.vn';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status, handler_id, response_note)
SELECT s.user_id, s.user_id, 'CERTIFICATE_REQUEST', 'Xin giay gioi thieu thuc tap OJT', 'Em can giay gioi thieu den cong ty phan mem FPT Software Can Tho.', 'APPROVED', a.user_id, 'Giay gioi thieu da duoc gui qua email sinh vien.'
FROM users s, users a
WHERE s.email = 'bttse194501@fpt.edu.vn' AND a.email = 'admin@fpt.edu.vn';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status, handler_id, response_note)
SELECT s.user_id, p.user_id, 'LEAVE_OF_ABSENCE', 'Xin phep nghi hoc 2 ngay do ve que', 'Gia dinh xin phep cho em Cuong nghi hoc 2 ngay cuoi tuan.', 'APPROVED', l.user_id, 'Da ghi nhan phep nghi.'
FROM users s, users p, users l
WHERE s.email = 'cle194502@fpt.edu.vn' AND p.email = 'phu_huynh_cuong@gmail.com' AND l.email = 'anhnv@fe.edu.vn';

-- 9. CLUBS SEED
INSERT INTO clubs (club_name, description, category, avatar_url, contact_email, member_count)
VALUES
    ('F-Code Club', 'Cau lac bo lap trinh hoc thuat hang dau truong FPT.', 'TECHNOLOGY', 'https://api.dicebear.com/7.x/identicon/svg?seed=fcode', 'fcode@fpt.edu.vn', 120),
    ('F-Beat Music Club', 'Cau lac bo am nhac, san choi nghe thuat cho sinh vien.', 'ARTS', 'https://api.dicebear.com/7.x/identicon/svg?seed=fbeat', 'fbeat@fpt.edu.vn', 85),
    ('Vovinam FPT Club', 'Cau lac bo Vo co truyen Vovinam FPT Can Tho.', 'SPORTS', 'https://api.dicebear.com/7.x/identicon/svg?seed=vovinam', 'vovinam@fpt.edu.vn', 150),
    ('F-Art Club', 'Cau lac bo my thuat, hoi hoa va thiet ke do hoa.', 'ARTS', 'https://api.dicebear.com/7.x/identicon/svg?seed=fart', 'fart@fpt.edu.vn', 65),
    ('F-Chess Club', 'Cau lac bo co vua va co tuong danh cho cac ky thu.', 'SPORTS', 'https://api.dicebear.com/7.x/identicon/svg?seed=fchess', 'fchess@fpt.edu.vn', 45),
    ('F-English Club', 'Cau lac bo Tieng Anh giao tiep va luyen thi IELTS.', 'ACADEMIC', 'https://api.dicebear.com/7.x/identicon/svg?seed=fenglish', 'fenglish@fpt.edu.vn', 180),
    ('F-Robotics Club', 'Cau lac bo nghien cuu robot, IoT va tu dong hoa.', 'TECHNOLOGY', 'https://api.dicebear.com/7.x/identicon/svg?seed=frobotics', 'frobotics@fpt.edu.vn', 70),
    ('F-Dance Club', 'Cau lac bo nhay hien dai, hip-hop va K-pop cover.', 'ARTS', 'https://api.dicebear.com/7.x/identicon/svg?seed=fdance', 'fdance@fpt.edu.vn', 95),
    ('F-Media Club', 'Cau lac bo truyen thong, quay phim, chup anh va san xuat video.', 'MEDIA', 'https://api.dicebear.com/7.x/identicon/svg?seed=fmedia', 'fmedia@fpt.edu.vn', 110),
    ('F-Basketball Club', 'Cau lac bo bong ro sinh vien FPT.', 'SPORTS', 'https://api.dicebear.com/7.x/identicon/svg?seed=fbasketball', 'fbasketball@fpt.edu.vn', 60),
    ('F-Badminton Club', 'Cau lac bo cau long ren luyen the luc va giao luu.', 'SPORTS', 'https://api.dicebear.com/7.x/identicon/svg?seed=fbadminton', 'fbadminton@fpt.edu.vn', 80),
    ('F-Volunteers Club', 'Doi thanh nien tinh nguyen vi cong dong FPT.', 'VOLUNTEER', 'https://api.dicebear.com/7.x/identicon/svg?seed=fvolunteer', 'fvolunteers@fpt.edu.vn', 210),
    ('F-Startup Club', 'Cau lac bo khoi nghiep va sang tao kinh doanh tre.', 'OTHER', 'https://api.dicebear.com/7.x/identicon/svg?seed=fstartup', 'fstartup@fpt.edu.vn', 55)
ON CONFLICT (club_name) DO NOTHING;

-- 10. CLUB_MEMBERSHIPS SEED
INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'MEMBER'
FROM clubs c, users s
WHERE c.club_name = 'F-Code Club' AND s.email = 'bttse194501@fpt.edu.vn'
ON CONFLICT (club_id, user_id) DO NOTHING;

INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'LEADER'
FROM clubs c, users s
WHERE c.club_name = 'F-Code Club' AND s.email = 'vananvu429@gmail.com'
ON CONFLICT (club_id, user_id) DO NOTHING;

INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'CORE_MEMBER'
FROM clubs c, users s
WHERE c.club_name = 'F-Code Club' AND s.email = 'cle194502@fpt.edu.vn'
ON CONFLICT (club_id, user_id) DO NOTHING;

INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'MEMBER'
FROM clubs c, users s
WHERE c.club_name = 'F-Beat Music Club' AND s.email = 'vananvu429@gmail.com'
ON CONFLICT (club_id, user_id) DO NOTHING;

INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'VICE_LEADER'
FROM clubs c, users s
WHERE c.club_name = 'F-Beat Music Club' AND s.email = 'bttse194501@fpt.edu.vn'
ON CONFLICT (club_id, user_id) DO NOTHING;

INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'MEMBER'
FROM clubs c, users s
WHERE c.club_name = 'Vovinam FPT Club' AND s.email = 'cle194502@fpt.edu.vn'
ON CONFLICT (club_id, user_id) DO NOTHING;

INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'LEADER'
FROM clubs c, users s
WHERE c.club_name = 'F-Art Club' AND s.email = 'dtmse194503@fpt.edu.vn'
ON CONFLICT (club_id, user_id) DO NOTHING;

INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'MEMBER'
FROM clubs c, users s
WHERE c.club_name = 'F-Chess Club' AND s.email = 'hoangnm194504@fpt.edu.vn'
ON CONFLICT (club_id, user_id) DO NOTHING;

INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'CORE_MEMBER'
FROM clubs c, users s
WHERE c.club_name = 'F-English Club' AND s.email = 'phuongnt194505@fpt.edu.vn'
ON CONFLICT (club_id, user_id) DO NOTHING;

INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'MEMBER'
FROM clubs c, users s
WHERE c.club_name = 'F-Robotics Club' AND s.email = 'minhnv194506@fpt.edu.vn'
ON CONFLICT (club_id, user_id) DO NOTHING;

INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'LEADER'
FROM clubs c, users s
WHERE c.club_name = 'F-Media Club' AND s.email = 'hahtt194508@fpt.edu.vn'
ON CONFLICT (club_id, user_id) DO NOTHING;

-- 11. NEWS SEED
INSERT INTO news (title, summary, content, thumbnail_url, category, views_count, is_published)
VALUES
    ('Lich thi Hoc ky Fall 2026 chinh thuc', 'Phong Dao tao thong bao lich thi cac mon chuyen nganh SE/PRM cho sinh vien.', 'Chi tiet ca thi va phong thi da duoc cap nhat tren he thong MyFSchool. Sinh vien luu y mang theo the sinh vien khi vao phong thi.', 'https://picsum.photos/seed/exam/800/400', 'ACADEMIC', 1420, true),
    ('FPT Edu Camp 2026 sap dien ra', 'Ngay hoi giao luu cong nghe va hoat dong ngoai khoa quy mo lon tai campus.', 'Tham gia ngay de trai nghiem cac gian hang cong nghe, cuoc thi lap trinh Hackathon va dem nhac hoi bung no.', 'https://picsum.photos/seed/camp/800/400', 'EVENT', 3200, true),
    ('Thong bao hoc bong FPT Talent 2026', 'Chuong trinh hoc bong danh cho cac sinh vien co thanh tich xuat sac.', 'Cac ban sinh vien dat GPA tren 8.5 vui long nop ho so truoc ngay 30/11 de duoc xet duyet hoc bong ky toi.', 'https://picsum.photos/seed/scholarship/800/400', 'SCHOLARSHIP', 980, true),
    ('Hoi thao Cong nghe AI va Xu huong Nghe nghiep 2027', 'Hoi thao voi su tham gia cua cac chuyen gia hang dau tu Google va OpenAI.', 'Chi tiet buoi hoi thao chia se ve ung dung AI Agents va mo hinh ngon ngu lon trong doanh nghiep hien dai.', 'https://picsum.photos/seed/ai-conf/800/400', 'ACADEMIC', 1850, true),
    ('Giai bong da sinh vien F-Cup 2026 khoi tranh', 'Giai dau the thao thu hut hon 32 doi bong tranh tai soi noi tai san van dong.', 'Lich thi dau vong bang F-Cup 2026 da duoc ban to chuc cong bo voi nhieu tran cau hap dan.', 'https://picsum.photos/seed/football/800/400', 'ACTIVITY', 2100, true),
    ('Chuong trinh trao doi sinh vien quoc te tai Nhat Ban', 'Co hoi hoc tap va trai nghiem van hoa danh cho sinh vien nganh CNTT.', 'Khoa CNTT thong bao mo don dang ky chuong trinh trao doi 1 ky tai Dai hoc Tokyo.', 'https://picsum.photos/seed/japan/800/400', 'SCHOLARSHIP', 1540, true),
    ('FPT Hackathon 2026: San choi lap trinh 48h khong ngu', 'Cuoc thi lap trinh thu hut hon 100 lap trinh vien tre thu suc voi de bai AI.', 'Tong gia tri giai thuong len den 200 trieu dong kem theo co hoi tuyen thang vao tap doan.', 'https://picsum.photos/seed/hackathon/800/400', 'EVENT', 3450, true),
    ('Thong bao nop hoc phi Hoc ky Spring 2027', 'Phong Ke toan thong bao han muc va thoi gian dong hoc phi ky toi.', 'Sinh vien hoan thanh hoc phi qua cong thanh toan truc tuyen truoc ngay 15/12 de giu cho lop hoc.', 'https://picsum.photos/seed/tuition/800/400', 'ACADEMIC', 2890, true),
    ('Khai truong Khong gian Sang tao va Khoi nghiep F-Hub', 'Khong gian mo hien dai phuc vu hoc tap, nghien cuu va ket noi du an start-up.', 'F-Hub trang bi day du thiet bi in 3D, he thong server test va khu vuc thao luan nhom.', 'https://picsum.photos/seed/fhub/800/400', 'EVENT', 1780, true),
    ('Dem nhac hoi F-Festival chao don tan sinh vien K22', 'Su kien am nhac quy mo hoanh trang voi su gop mat cua nhieu ca si noi tieng.', 'Dem hoi bung no cam xuc voi am thanh, anh sang tieu chuan quoc te tai quang truong campus.', 'https://picsum.photos/seed/festival/800/400', 'EVENT', 4500, true),
    ('Chien dich Tinh nguyen Mua He Xanh 2026 thanh cong tot dep', 'Hon 200 sinh vien FPT da hoan thanh cong trinh cau duong va day hoc tai vung sau.', 'Nhung nu cuoi va trai nghiem quy gia sau 3 tuan dong hanh cung ba con vung dong bang.', 'https://picsum.photos/seed/volunteer/800/400', 'ACTIVITY', 1230, true),
    ('Hoc bong Doanh nghiep CMC Telecom danh cho sinh vien SE', 'Hoc bong toan phan ho tro hoc phi va cam ket viec lam sau khi tot nghiep.', 'Tieu chi xet tuyen uu tien cac sinh vien co chung chi quoc te va GPA tren 8.0.', 'https://picsum.photos/seed/cmc/800/400', 'SCHOLARSHIP', 990, true),
    ('Quy dinh ve trang phuc va van hoa hoc duong nam hoc moi', 'Nha truong ban hanh bo quy tac ung xu va trang phuc van minh trong khuon vien.', 'Sinh vien can tuan thu deo the sinh vien va mac dong phuc theo dung lich quy dinh.', 'https://picsum.photos/seed/culture/800/400', 'GENERAL', 1120, true)
ON CONFLICT DO NOTHING;

-- 12. NOTIFICATIONS SEED
INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Lịch thi Học kỳ Fall 2026 chính thức', 'Phòng Đào tạo đã công bố lịch thi các môn chuyên ngành SE/PRM.', 'NEWS', 1, false, now() - interval '2 hour'
FROM users u
WHERE u.role = 'STUDENT';

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'FPT Edu Camp 2026 sắp diễn ra', 'Ngày hội giao lưu công nghệ và hoạt động ngoại khóa quy mô lớn tại campus.', 'NEWS', 2, false, now() - interval '1 day'
FROM users u
WHERE u.role = 'STUDENT';

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Bảo mật: Mật khẩu đã được cập nhật', 'Mật khẩu tài khoản của bạn đã được thay đổi thành công vào lúc 10:00.', 'SECURITY', null, true, now() - interval '3 day'
FROM users u
WHERE u.email IN ('vananvu429@gmail.com', 'bttse194501@fpt.edu.vn');

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Nhắc nhở học vụ: Nộp đơn xét tốt nghiệp', 'Hạn chót nộp đơn xét tốt nghiệp đợt 3 là ngày 15/11.', 'ACADEMIC', null, false, now() - interval '4 hour'
FROM users u
WHERE u.email = 'vananvu429@gmail.com';

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Học bổng CMC Telecom 2026', 'Hồ sơ xét tuyển học bổng doanh nghiệp CMC của bạn đã được tiếp nhận.', 'ACADEMIC', null, false, now() - interval '6 hour'
FROM users u
WHERE u.email = 'vananvu429@gmail.com';

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Kết quả đơn xin xác nhận sinh viên', 'Đơn xin giấy xác nhận sinh viên của bạn đã được Ban Đào tạo phê duyệt.', 'REQUEST', null, false, now() - interval '10 hour'
FROM users u
WHERE u.email = 'vananvu429@gmail.com';

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Điểm đánh giá Lab 1 đã được công bố', 'Giảng viên đã chấm điểm môn PRM393 lớp SE1945.', 'ACADEMIC', null, false, now() - interval '8 hour'
FROM users u
WHERE u.email = 'bttse194501@fpt.edu.vn';

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Cảnh báo điểm danh môn SWR302', 'Bạn đã vắng 1 buổi học môn Software Requirement.', 'ACADEMIC', null, false, now() - interval '1 day'
FROM users u
WHERE u.email = 'cle194502@fpt.edu.vn';

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Chào mừng thành viên CLB F-Art', 'Bạn đã được bổ nhiệm làm Trưởng nhóm CLB Mỹ thuật F-Art.', 'SYSTEM', null, true, now() - interval '2 day'
FROM users u
WHERE u.email = 'dtmse194503@fpt.edu.vn';

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Phản hồi đơn phúc khảo môn học', 'Đơn phúc khảo của bạn đang được phân công cho giảng viên chấm lại.', 'REQUEST', null, false, now() - interval '5 hour'
FROM users u
WHERE u.email = 'hoangnm194504@fpt.edu.vn';

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Thông báo tình hình học tập tuần 42', 'Sinh viên Trần Thị Bảo có tỷ lệ chuyên cần 100% trong tuần qua.', 'ACADEMIC', null, false, now() - interval '1 day'
FROM users u
WHERE u.email = 'phu_huynh_bao@gmail.com';

INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
SELECT u.user_id, 'Biên lai học phí học kỳ Fall 2026', 'Hóa đơn học phí học kỳ Fall 2026 đã được gạch nợ thành công.', 'SYSTEM', null, true, now() - interval '4 day'
FROM users u
WHERE u.email = 'phu_huynh_cuong@gmail.com';

-- Broadcast Notifications (user_id IS NULL)
INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
VALUES
    (NULL, 'Chào mừng năm học mới 2026 - 2027', 'Nhà trường chúc toàn thể sinh viên, giảng viên và phụ huynh một năm học mới nhiều thành công.', 'SYSTEM', null, false, now() - interval '5 day'),
    (NULL, 'Bảo trì hệ thống MyFSchool định kỳ', 'Hệ thống sẽ tạm dừng phục vụ trong 30 phút từ 02:00 đến 02:30 ngày Chủ nhật để nâng cấp CSDL.', 'SYSTEM', null, false, now() - interval '12 hour'),
    (NULL, 'Thông báo nghỉ lễ ngày 20/11', 'Toàn thể sinh viên và cán bộ giảng viên được nghỉ học và làm việc nhân ngày Nhà giáo Việt Nam.', 'SYSTEM', null, false, now() - interval '2 day');

-- 13. NOTIFICATION_READS SEED
INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '1 day'
FROM notifications n, users u
WHERE n.user_id IS NULL AND u.email = 'vananvu429@gmail.com'
ON CONFLICT (notification_id, user_id) DO NOTHING;

INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '1 day'
FROM notifications n, users u
WHERE n.title = 'Chào mừng năm học mới 2026 - 2027' AND u.email = 'bttse194501@fpt.edu.vn'
ON CONFLICT (notification_id, user_id) DO NOTHING;

INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '1 day'
FROM notifications n, users u
WHERE n.title = 'Chào mừng năm học mới 2026 - 2027' AND u.email = 'cle194502@fpt.edu.vn'
ON CONFLICT (notification_id, user_id) DO NOTHING;

INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '12 hour'
FROM notifications n, users u
WHERE n.title = 'Chào mừng năm học mới 2026 - 2027' AND u.email = 'dtmse194503@fpt.edu.vn'
ON CONFLICT (notification_id, user_id) DO NOTHING;

INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '12 hour'
FROM notifications n, users u
WHERE n.title = 'Chào mừng năm học mới 2026 - 2027' AND u.email = 'hoangnm194504@fpt.edu.vn'
ON CONFLICT (notification_id, user_id) DO NOTHING;

INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '6 hour'
FROM notifications n, users u
WHERE n.title = 'Bảo trì hệ thống MyFSchool định kỳ' AND u.email = 'vananvu429@gmail.com'
ON CONFLICT (notification_id, user_id) DO NOTHING;

INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '6 hour'
FROM notifications n, users u
WHERE n.title = 'Bảo trì hệ thống MyFSchool định kỳ' AND u.email = 'bttse194501@fpt.edu.vn'
ON CONFLICT (notification_id, user_id) DO NOTHING;

INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '5 hour'
FROM notifications n, users u
WHERE n.title = 'Bảo trì hệ thống MyFSchool định kỳ' AND u.email = 'cle194502@fpt.edu.vn'
ON CONFLICT (notification_id, user_id) DO NOTHING;

INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '1 day'
FROM notifications n, users u
WHERE n.title = 'Thông báo nghỉ lễ ngày 20/11' AND u.email = 'vananvu429@gmail.com'
ON CONFLICT (notification_id, user_id) DO NOTHING;

INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '1 day'
FROM notifications n, users u
WHERE n.title = 'Thông báo nghỉ lễ ngày 20/11' AND u.email = 'bttse194501@fpt.edu.vn'
ON CONFLICT (notification_id, user_id) DO NOTHING;

INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '2 day'
FROM notifications n, users u
WHERE n.title = 'Chào mừng năm học mới 2026 - 2027' AND u.email = 'anhnv@fe.edu.vn'
ON CONFLICT (notification_id, user_id) DO NOTHING;

-- 14. ATTENDANCES SEED
INSERT INTO attendances (schedule_id, student_id, status, note, recorded_by, recorded_at)
SELECT s.schedule_id, ce.student_id,
  CASE 
    WHEN s.status = 'PRESENT' THEN 'PRESENT'
    WHEN s.status = 'ABSENT' THEN 'ABSENT'
    ELSE 'PRESENT'
  END,
  'Điểm danh hệ thống tự động',
  s.lecturer_id,
  s.date + s.start_time
FROM schedules s
JOIN class_enrollments ce ON s.class_id = ce.class_id
WHERE s.status IN ('PRESENT', 'ABSENT', 'COMPLETED')
ON CONFLICT (schedule_id, student_id) DO NOTHING;


