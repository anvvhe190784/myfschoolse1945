export type UserRole = 'ADMIN' | 'LECTURER' | 'STUDENT' | 'PARENT'

export interface User {
  userId: number
  rollNumber?: string
  email: string
  fullName: string
  phoneNumber?: string
  avatarUrl?: string
  role: UserRole
  status?: 'ACTIVE' | 'INACTIVE' | 'LOCKED' | string
  mustChangePassword?: boolean
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userId: number
  rollNumber?: string
  email: string
  fullName: string
  role: string
  mustChangePassword?: boolean
}

export interface Course {
  courseId: number
  courseCode: string
  courseName: string
  credits: number
  description?: string
}

export interface ClassItem {
  classId: number
  classCode: string
  courseCode: string
  courseName: string
  lecturerName?: string
  semester: string
  academicYear: number
  roomDefault?: string
}

export interface ScheduleItem {
  scheduleId: number
  classId: number
  classCode: string
  courseCode: string
  courseName: string
  slotNumber: number
  sessionNo: number
  date: string
  startTime: string
  endTime: string
  room: string
  lecturerName: string
  note?: string
  status: string
  semester?: string
}

export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE'

export interface AttendanceStudentItem {
  studentId: number
  rollNumber: string
  fullName: string
  status?: AttendanceStatus | string
  note?: string
}

export interface ScheduleAttendanceResponse {
  scheduleId: number
  classId: number
  classCode: string
  courseCode: string
  date: string
  slotNumber: number
  students: AttendanceStudentItem[]
  totalStudents: number
  presentCount: number
  absentCount: number
  lateCount: number
}

export interface AttendanceRecord {
  studentId: number
  studentRollNumber: string
  studentName: string
  status: AttendanceStatus
  note?: string
  avatarUrl?: string
}

export interface GradeItem {
  gradeId: number
  classId: number
  classCode: string
  courseName: string
  studentId?: number
  studentName?: string
  studentRollNumber?: string
  assessmentName: string
  weight: number
  score: number
  feedback?: string
  status: string
}

export interface RequestItem {
  requestId: number
  studentId: number
  studentName: string
  studentRollNumber: string
  requestType: string
  title: string
  content: string
  attachmentUrl?: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | string
  responseNote?: string
  createdAt: string
}

export interface NewsItem {
  newsId: number
  title: string
  summary: string
  content: string
  thumbnailUrl?: string
  authorName?: string
  category?: string
  viewsCount?: number
  publishedAt: string
}

export interface ClubItem {
  clubId: number
  clubName: string
  description: string
  category: string
  avatarUrl?: string
  bannerUrl?: string
  leaderName?: string
  contactEmail?: string
  memberCount: number
  isActive: boolean
}

export interface NotificationItem {
  notificationId: number
  userId?: number
  title: string
  content: string
  type: string
  targetId?: number
  isRead: boolean
  createdAt: string
}

export interface ImportResult {
  total: number
  successCount: number
  failureCount: number
  errors?: string[]
}
