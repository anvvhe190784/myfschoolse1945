-- Clean and reseed notifications with clean UTF-8
TRUNCATE notification_reads, notifications RESTART IDENTITY CASCADE;

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
