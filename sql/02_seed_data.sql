-- ==============================================================================
-- Seed Data for F-School System (PostgreSQL 18)
-- Includes realistic test accounts for Students, Lecturers, Parents, and Admin
-- ==============================================================================

-- 1. USERS SEED
INSERT INTO users (roll_number, email, password_hash, full_name, phone_number, avatar_url, role, status)
VALUES
    -- Admin
    ('AD001', 'admin@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'System Administrator', '0901234567', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 'ADMIN', 'ACTIVE'),
    -- Lecturer
    ('GV001', 'anhnv@fe.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Nguyen Van Anh', '0912345678', 'https://api.dicebear.com/7.x/avataaars/svg?seed=lecturer1', 'LECTURER', 'ACTIVE'),
    -- Students
    ('HE190784', 'vananvu429@gmail.com', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Vũ Văn An', '0912000111', 'https://api.dicebear.com/7.x/avataaars/svg?seed=HE190784', 'STUDENT', 'ACTIVE'),
    ('SE194501', 'bttse194501@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Tran Thi Bao', '0987654321', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student1', 'STUDENT', 'ACTIVE'),
    ('SE194502', 'cle194502@fpt.edu.vn', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Le Van Cuong', '0978123456', 'https://api.dicebear.com/7.x/avataaars/svg?seed=student2', 'STUDENT', 'ACTIVE'),
    -- Parents
    ('PH001', 'phu_huynh_bao@gmail.com', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Tran Van Dung', '0933112233', 'https://api.dicebear.com/7.x/avataaars/svg?seed=parent1', 'PARENT', 'ACTIVE'),
    ('PH002', 'phu_huynh_cuong@gmail.com', '$2a$08$89zvFyr3enJTvlmvtN81z.V46nyiKR6QY.KVnPuV3NOCUi6jIuptK', 'Pham Thi Hoa', '0944556677', 'https://api.dicebear.com/7.x/avataaars/svg?seed=parent2', 'PARENT', 'ACTIVE')
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

-- 3. COURSES SEED
INSERT INTO courses (course_code, course_name, credits, description)
VALUES
    ('PRM393', 'Mobile Programming (Android/Flutter)', 3, 'Lap trinh ung dung di dong nang cao tren nen tang Flutter va Android Native.'),
    ('SWR302', 'Software Requirement', 3, 'Khao sat, thu thap va dac ta yeu cau phan mem theo quy trinh chuan.'),
    ('SWP391', 'Software Development Project', 3, 'Du an phat trien phan mem thuc te theo mo hinh Scrum.')
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

-- 6. SCHEDULES SEED (No meet_url, with session_no)
INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 2, '2026-10-12', '10:00:00', '12:20:00', 'AL-R402', 2, cl.lecturer_id, 'Session 2: MLN122 Overview', 'ABSENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393';

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 1, '2026-10-13', '07:30:00', '09:50:00', 'BE-211', 11, cl.lecturer_id, 'Session 11: Flutter UI Testing', 'PRESENT'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393';

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 1, '2026-10-14', '07:30:00', '09:50:00', 'BE-301', 12, cl.lecturer_id, 'Session 12: Flutter Architecture & Database', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'PRM393';

INSERT INTO schedules (class_id, slot_number, date, start_time, end_time, room, session_no, lecturer_id, note, status)
SELECT cl.class_id, 3, '2026-10-16', '10:00:00', '12:20:00', 'BE-305', 1, cl.lecturer_id, 'Session 1: Introduction to Requirements Engineering', 'UPCOMING'
FROM classes cl JOIN courses c ON cl.course_id = c.course_id WHERE cl.class_code = 'SE1945' AND c.course_code = 'SWR302';

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

-- 8. REQUESTS SEED (Đơn từ)
INSERT INTO requests (student_id, requester_id, request_type, title, content, status, response_note)
SELECT s.user_id, p.user_id, 'LEAVE_OF_ABSENCE', 'Don xin phep nghi hoc vi ly do suc khoe', 'Kinh gui thay chu nhiem, toi la phu huynh em Bao xin phep cho em nghi buoi hoc ngay mai.', 'APPROVED', 'Da ghi nhan don, chuc em mau khoe.'
FROM users s, users p
WHERE s.email = 'bttse194501@fpt.edu.vn' AND p.email = 'phu_huynh_bao@gmail.com';

INSERT INTO requests (student_id, requester_id, request_type, title, content, status)
SELECT s.user_id, s.user_id, 'GRADE_REVIEW', 'Don phuc khao diem Progress Test 1', 'Em kinh de nghi xem lai cau hoi so 3 trong bai kiem tra.', 'PENDING'
FROM users s
WHERE s.email = 'cle194502@fpt.edu.vn';

-- 9. CLUBS SEED
INSERT INTO clubs (club_name, description, category, avatar_url, contact_email, member_count)
VALUES
    ('F-Code Club', 'Cau lac bo lap trinh hoc thuat hang dau truong FPT.', 'TECHNOLOGY', 'https://api.dicebear.com/7.x/identicon/svg?seed=fcode', 'fcode@fpt.edu.vn', 120),
    ('F-Beat Music Club', 'Cau lac bo am nhac, san choi nghe thuat cho sinh vien.', 'ARTS', 'https://api.dicebear.com/7.x/identicon/svg?seed=fbeat', 'fbeat@fpt.edu.vn', 85),
    ('Vovinam FPT Club', 'Cau lac bo Vo co truyen Vovinam FPT Can Tho.', 'SPORTS', 'https://api.dicebear.com/7.x/identicon/svg?seed=vovinam', 'vovinam@fpt.edu.vn', 150)
ON CONFLICT (club_name) DO NOTHING;

-- 10. CLUB_MEMBERSHIPS SEED
INSERT INTO club_memberships (club_id, user_id, role)
SELECT c.club_id, s.user_id, 'MEMBER'
FROM clubs c, users s
WHERE c.club_name = 'F-Code Club' AND s.email = 'bttse194501@fpt.edu.vn'
ON CONFLICT (club_id, user_id) DO NOTHING;

-- 11. NEWS SEED
INSERT INTO news (title, summary, content, thumbnail_url, category, views_count, is_published)
VALUES
    ('Lich thi Hoc ky Fall 2026 chinh thuc', 'Phong Dao tao thong bao lich thi cac mon chuyen nganh SE/PRM cho sinh vien.', 'Chi tiet ca thi va phong thi da duoc cap nhat tren he thong MyFSchool. Sinh vien luu y mang theo the sinh vien khi vao phong thi.', 'https://picsum.photos/seed/exam/800/400', 'ACADEMIC', 1420, true),
    ('FPT Edu Camp 2026 sap dien ra', 'Ngay hoi giao luu cong nghe va hoat dong ngoai khoa quy mo lon tai campus.', 'Tham gia ngay de trai nghiem cac gian hang cong nghe, cuoc thi lap trinh Hackathon va dem nhac hoi bung no.', 'https://picsum.photos/seed/camp/800/400', 'EVENT', 3200, true),
    ('Thong bao hoc bong FPT Talent 2026', 'Chuong trinh hoc bong danh cho cac sinh vien co thanh tich xuat sac.', 'Cac ban sinh vien dat GPA tren 8.5 vui long nop ho so truoc ngay 30/11 de duoc xet duyet hoc bong ky toi.', 'https://picsum.photos/seed/scholarship/800/400', 'SCHOLARSHIP', 980, true);

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

-- Broadcast Notification (user_id IS NULL)
INSERT INTO notifications (user_id, title, content, type, target_id, is_read, created_at)
VALUES
    (NULL, 'Chào mừng năm học mới 2026 - 2027', 'Nhà trường chúc toàn thể sinh viên, giảng viên và phụ huynh một năm học mới nhiều thành công.', 'SYSTEM', null, false, now() - interval '5 day');

-- 13. NOTIFICATION_READS SEED
INSERT INTO notification_reads (notification_id, user_id, read_at)
SELECT n.notification_id, u.user_id, now() - interval '1 day'
FROM notifications n, users u
WHERE n.user_id IS NULL AND u.email = 'vananvu429@gmail.com'
ON CONFLICT (notification_id, user_id) DO NOTHING;

