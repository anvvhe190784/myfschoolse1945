-- ==============================================================================
-- Schema & Seed for Attendances (PostgreSQL 16+)
-- ==============================================================================

-- 1. ATTENDANCES TABLE
CREATE TABLE IF NOT EXISTS attendances (
    attendance_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    schedule_id BIGINT NOT NULL REFERENCES schedules(schedule_id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'PRESENT' CHECK (status IN ('PRESENT', 'ABSENT', 'LATE')),
    note TEXT,
    recorded_by BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_schedule_student_attendance UNIQUE (schedule_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_attendances_schedule ON attendances (schedule_id);
CREATE INDEX IF NOT EXISTS idx_attendances_student ON attendances (student_id);

-- 2. ATTENDANCES SEED
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
