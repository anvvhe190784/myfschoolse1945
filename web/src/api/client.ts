import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { message } from 'antd'

export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  errors?: Record<string, string> | string[]
  [key: string]: unknown
}

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request Interceptor: Attach JWT Bearer Token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('fschool_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response Interceptor: Handle RFC 7807 ProblemDetail & Auth Expire
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ProblemDetail>) => {
    if (error.response) {
      const { status, data } = error.response
      
      // Parse RFC 7807 detail message
      let errorMessage = 'Đã có lỗi xảy ra. Vui lòng thử lại!'
      if (data) {
        if (data.detail) {
          errorMessage = data.detail
        } else if (data.title) {
          errorMessage = data.title
        } else if (typeof data === 'string') {
          errorMessage = data
        } else if ((data as { message?: string }).message) {
          errorMessage = (data as { message?: string }).message!
        }

        // If field-specific errors exist
        if (data.errors && typeof data.errors === 'object') {
          const fieldMsgs = Array.isArray(data.errors) 
            ? data.errors.join(', ')
            : Object.values(data.errors).join(', ')
          if (fieldMsgs) {
            errorMessage += `: ${fieldMsgs}`
          }
        }
      }

      if (status === 401) {
        const isLoginRequest = error.config?.url?.includes('/auth/login')
        if (!isLoginRequest) {
          message.error('Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.')
          localStorage.removeItem('fschool_token')
          localStorage.removeItem('fschool_refresh_token')
          localStorage.removeItem('fschool_user')
          if (window.location.pathname !== '/login') {
            window.location.href = '/login'
          }
        }
      } else if (status === 403) {
        message.error('Bạn không có quyền thực hiện thao tác này.')
      }

      // Attach formatted message to error object for easy consumption
      (error as unknown as { rfcMessage: string }).rfcMessage = errorMessage
    } else if (error.request) {
      const netMsg = 'Không thể kết nối đến máy chủ backend (http://localhost:8080). Vui lòng kiểm tra dịch vụ!'
      ;(error as unknown as { rfcMessage: string }).rfcMessage = netMsg
    }

    return Promise.reject(error)
  }
)

export default apiClient
