import React from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { Spin } from 'antd'
import { useAuth } from '@/contexts/AuthContext'

interface ProtectedRouteProps {
  children: React.ReactElement
  allowedRoles?: ('ADMIN' | 'LECTURER')[]
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, allowedRoles }) => {
  const { isAuthenticated, role, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
          background: '#F0F2F5',
        }}
      >
        <Spin size="large" tip="Đang tải phiên đăng nhập F-School..." />
      </div>
    )
  }

  if (!isAuthenticated || !role) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(role)) {
    // Redirect to default home page according to role
    if (role === 'ADMIN') {
      return <Navigate to="/admin/dashboard" replace />
    } else if (role === 'LECTURER') {
      return <Navigate to="/teacher/schedule" replace />
    }
    return <Navigate to="/login" replace />
  }

  return children
}

export default ProtectedRoute
