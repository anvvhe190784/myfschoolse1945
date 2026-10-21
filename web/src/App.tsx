import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ConfigProvider, Result, Button } from 'antd'
import viVN from 'antd/locale/vi_VN'
import dayjs from 'dayjs'
import 'dayjs/locale/vi'

import { AuthProvider, useAuth } from '@/contexts/AuthContext'
import ProtectedRoute from '@/components/ProtectedRoute'
import MainLayout from '@/layouts/MainLayout'

// Pages
import LoginPage from '@/pages/auth/LoginPage'

// Common Pages
import NotificationsPage from '@/pages/common/NotificationsPage'
import NewsPage from '@/pages/common/NewsPage'
import NewsDetailPage from '@/pages/common/NewsDetailPage'
import ClubPage from '@/pages/common/ClubPage'

// Lecturer Pages
import TeacherSchedulePage from '@/pages/teacher/TeacherSchedulePage'
import AttendancePage from '@/pages/teacher/AttendancePage'
import TeacherGradePage from '@/pages/teacher/TeacherGradePage'

// Admin Pages
import AdminDashboardPage from '@/pages/admin/AdminDashboardPage'
import UserManagementPage from '@/pages/admin/UserManagementPage'
import CourseManagementPage from '@/pages/admin/CourseManagementPage'
import ClassManagementPage from '@/pages/admin/ClassManagementPage'
import ScheduleManagementPage from '@/pages/admin/ScheduleManagementPage'
import RequestManagementPage from '@/pages/admin/RequestManagementPage'
import BulkImportPage from '@/pages/admin/BulkImportPage'
import NewsManagementPage from '@/pages/admin/NewsManagementPage'

dayjs.locale('vi')

const RootRedirect: React.FC = () => {
  const { isAuthenticated, role, loading } = useAuth()

  if (loading) return null

  if (!isAuthenticated || !role) {
    return <Navigate to="/login" replace />
  }

  if (role === 'ADMIN') {
    return <Navigate to="/admin/dashboard" replace />
  }

  return <Navigate to="/teacher/schedule" replace />
}

export const App: React.FC = () => {
  return (
    <ConfigProvider
      locale={viVN}
      theme={{
        token: {
          colorPrimary: '#F26F21',
          colorInfo: '#1890FF',
          colorSuccess: '#52C41A',
          colorWarning: '#FAAD14',
          colorError: '#F5222D',
          borderRadius: 8,
          fontFamily: `'Segoe UI', -apple-system, BlinkMacSystemFont, Roboto, 'Helvetica Neue', Arial, sans-serif`,
        },
      }}
    >
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            {/* Public route */}
            <Route path="/login" element={<LoginPage />} />

            {/* Protected Routes wrapped in MainLayout */}
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <MainLayout />
                </ProtectedRoute>
              }
            >
              <Route index element={<RootRedirect />} />

              {/* Common Portal Routes */}
              <Route path="notifications" element={<NotificationsPage />} />
              <Route path="news" element={<NewsPage />} />
              <Route path="news/:id" element={<NewsDetailPage />} />
              <Route path="clubs" element={<ClubPage />} />

              {/* Lecturer Routes (accessible by LECTURER and ADMIN) */}
              <Route
                path="teacher/schedule"
                element={
                  <ProtectedRoute allowedRoles={['LECTURER', 'ADMIN']}>
                    <TeacherSchedulePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="teacher/attendance"
                element={
                  <ProtectedRoute allowedRoles={['LECTURER', 'ADMIN']}>
                    <AttendancePage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="teacher/grades"
                element={
                  <ProtectedRoute allowedRoles={['LECTURER', 'ADMIN']}>
                    <TeacherGradePage />
                  </ProtectedRoute>
                }
              />

              {/* Admin Routes (strictly ADMIN only) */}
              <Route
                path="admin/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <AdminDashboardPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="admin/users"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <UserManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="admin/courses"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <CourseManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="admin/classes"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <ClassManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="admin/schedules"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <ScheduleManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="admin/requests"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <RequestManagementPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="admin/import"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <BulkImportPage />
                  </ProtectedRoute>
                }
              />
              <Route
                path="admin/news"
                element={
                  <ProtectedRoute allowedRoles={['ADMIN']}>
                    <NewsManagementPage />
                  </ProtectedRoute>
                }
              />
            </Route>

            {/* 404 Fallback */}
            <Route
              path="*"
              element={
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    minHeight: '100vh',
                    background: '#F0F2F5',
                  }}
                >
                  <Result
                    status="404"
                    title="404"
                    subTitle="Xin lỗi, trang bạn đang tìm kiếm không tồn tại trên Cổng F-School."
                    extra={
                      <Button
                        type="primary"
                        onClick={() => (window.location.href = '/')}
                        style={{ background: '#F26F21', borderColor: '#F26F21' }}
                      >
                        Quay lại Trang chủ
                      </Button>
                    }
                  />
                </div>
              }
            />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ConfigProvider>
  )
}

export default App
