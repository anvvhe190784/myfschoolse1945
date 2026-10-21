import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import apiClient from '@/api/client'
import { User, LoginResponse } from '@/types'

interface AuthContextType {
  user: User | null
  token: string | null
  refreshToken: string | null
  role: 'ADMIN' | 'LECTURER' | null
  isAuthenticated: boolean
  loading: boolean
  login: (data: LoginResponse) => void
  logout: () => Promise<void>
  updateUser: (patch: Partial<User>) => void
  refreshSession: () => Promise<boolean>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

const TOKEN_KEY = 'fschool_token'
const REFRESH_TOKEN_KEY = 'fschool_refresh_token'
const USER_KEY = 'fschool_user'

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [refreshToken, setRefreshToken] = useState<string | null>(() => localStorage.getItem(REFRESH_TOKEN_KEY))
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem(USER_KEY)
    if (saved) {
      try {
        return JSON.parse(saved)
      } catch {
        return null
      }
    }
    return null
  })
  const [loading, setLoading] = useState<boolean>(true)

  useEffect(() => {
    // Validate initial storage state
    const savedToken = localStorage.getItem(TOKEN_KEY)
    const savedUser = localStorage.getItem(USER_KEY)
    if (savedToken && savedUser) {
      try {
        const parsed = JSON.parse(savedUser)
        setUser(parsed)
        setToken(savedToken)
      } catch {
        clearAuth()
      }
    }
    setLoading(false)
  }, [])

  const clearAuth = () => {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(REFRESH_TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    setToken(null)
    setRefreshToken(null)
    setUser(null)
  }

  const login = (data: LoginResponse) => {
    const newUser: User = {
      userId: data.userId,
      rollNumber: data.rollNumber,
      email: data.email,
      fullName: data.fullName,
      role: data.role as 'ADMIN' | 'LECTURER',
      mustChangePassword: data.mustChangePassword,
    }

    localStorage.setItem(TOKEN_KEY, data.accessToken)
    if (data.refreshToken) {
      localStorage.setItem(REFRESH_TOKEN_KEY, data.refreshToken)
    }
    localStorage.setItem(USER_KEY, JSON.stringify(newUser))

    setToken(data.accessToken)
    setRefreshToken(data.refreshToken || null)
    setUser(newUser)
  }

  const logout = async () => {
    try {
      const currentRefresh = localStorage.getItem(REFRESH_TOKEN_KEY)
      await apiClient.post('/auth/logout', { refreshToken: currentRefresh })
    } catch {
      // Ignore network/server errors during logout
    } finally {
      clearAuth()
      window.location.href = '/login'
    }
  }

  const updateUser = (patch: Partial<User>) => {
    if (!user) return
    const updated = { ...user, ...patch }
    setUser(updated)
    localStorage.setItem(USER_KEY, JSON.stringify(updated))
  }

  const refreshSession = async (): Promise<boolean> => {
    const storedRefresh = localStorage.getItem(REFRESH_TOKEN_KEY)
    if (!storedRefresh) {
      clearAuth()
      return false
    }

    try {
      const response = await apiClient.post<{
        accessToken: string
        refreshToken: string
        expiresIn: number
      }>('/auth/refresh-token', { refreshToken: storedRefresh })

      const { accessToken, refreshToken: newRefresh } = response.data
      localStorage.setItem(TOKEN_KEY, accessToken)
      setToken(accessToken)

      if (newRefresh) {
        localStorage.setItem(REFRESH_TOKEN_KEY, newRefresh)
        setRefreshToken(newRefresh)
      }
      return true
    } catch {
      clearAuth()
      return false
    }
  }

  const validRole = user?.role === 'ADMIN' || user?.role === 'LECTURER' ? (user.role as 'ADMIN' | 'LECTURER') : null
  const isAuthenticated = !!token && !!user && !!validRole

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        refreshToken,
        role: validRole,
        isAuthenticated,
        loading,
        login,
        logout,
        updateUser,
        refreshSession,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
