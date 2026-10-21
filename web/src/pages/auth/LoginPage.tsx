import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Card,
  Form,
  Input,
  Button,
  Typography,
  Space,
  Modal,
  Alert,
} from 'antd'
import {
  UserOutlined,
  LockOutlined,
  LoginOutlined,
  MobileOutlined,
  CheckCircleFilled,
} from '@ant-design/icons'
import apiClient from '@/api/client'
import { useAuth } from '@/contexts/AuthContext'
import { LoginResponse } from '@/types'

const { Title, Text, Paragraph } = Typography

export const LoginPage: React.FC = () => {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [loading, setLoading] = useState(false)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [studentModalVisible, setStudentModalVisible] = useState(false)

  const handleLogin = async (values: { identifier: string; password: string }) => {
    setErrorMsg(null)
    setLoading(true)
    try {
      const response = await apiClient.post<LoginResponse>('/auth/login', {
        identifier: values.identifier.trim(),
        password: values.password,
      })

      const data = response.data

      // Block STUDENT role and show mobile app instruction modal
      if (data.role === 'STUDENT' || data.role === 'PARENT') {
        setStudentModalVisible(true)
        setLoading(false)
        return
      }

      // Check if user has ADMIN or LECTURER role
      if (data.role === 'ADMIN') {
        login(data)
        void navigate('/admin/dashboard')
      } else if (data.role === 'LECTURER') {
        login(data)
        void navigate('/teacher/schedule')
      } else {
        setErrorMsg('Tài khoản của bạn không có quyền truy cập hệ thống này.')
      }
    } catch (err: unknown) {
      const msg =
        (err as { rfcMessage?: string })?.rfcMessage ||
        'Đăng nhập thất bại. Vui lòng kiểm tra lại Email và Mật khẩu!'
      setErrorMsg(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      style={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #001529 0%, #002766 50%, #003a8c 100%)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '24px',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      {/* Decorative background glow */}
      <div
        style={{
          position: 'absolute',
          top: '-10%',
          right: '-5%',
          width: 500,
          height: 500,
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(242, 111, 33, 0.25) 0%, rgba(242, 111, 33, 0) 70%)',
          pointerEvents: 'none',
        }}
      />
      <div
        style={{
          position: 'absolute',
          bottom: '-10%',
          left: '-5%',
          width: 500,
          height: 500,
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(24, 144, 255, 0.2) 0%, rgba(24, 144, 255, 0) 70%)',
          pointerEvents: 'none',
        }}
      />

      <Card
        style={{
          width: 440,
          maxWidth: '100%',
          borderRadius: 16,
          boxShadow: '0 20px 40px rgba(0, 0, 0, 0.3)',
          border: '1px solid rgba(255, 255, 255, 0.1)',
          backdropFilter: 'blur(10px)',
          background: '#ffffff',
          overflow: 'hidden',
        }}
        styles={{ body: { padding: '40px 36px' } }}
      >
        {/* Brand Header */}
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div
            style={{
              width: 56,
              height: 56,
              borderRadius: 14,
              background: 'linear-gradient(135deg, #F26F21 0%, #FA8C16 100%)',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
              fontSize: 28,
              fontWeight: 800,
              boxShadow: '0 8px 16px rgba(242, 111, 33, 0.35)',
              marginBottom: 16,
            }}
          >
            F
          </div>
          <Title level={3} style={{ margin: 0, color: '#002766', fontWeight: 800 }}>
            F-SCHOOL PORTAL
          </Title>
          <Text type="secondary" style={{ fontSize: 13, marginTop: 4, display: 'block' }}>
            Cổng Thông Tin Dành Cho Quản Trị & Giảng Viên
          </Text>
        </div>

        {errorMsg && (
          <Alert
            message={errorMsg}
            type="error"
            showIcon
            closable
            onClose={() => setErrorMsg(null)}
            style={{ marginBottom: 20 }}
          />
        )}

        <Form layout="vertical" onFinish={handleLogin} size="large" requiredMark={false}>
          <Form.Item
            name="identifier"
            rules={[
              { required: true, message: 'Vui lòng nhập Email FPT!' },
              { type: 'email', message: 'Định dạng email không hợp lệ!' },
            ]}
          >
            <Input
              prefix={<UserOutlined style={{ color: '#bfbfbf' }} />}
              placeholder="Email FPT (@fpt.edu.vn / @fe.edu.vn)"
              autoFocus
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Vui lòng nhập mật khẩu tài khoản!' }]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: '#bfbfbf' }} />}
              placeholder="Mật khẩu"
            />
          </Form.Item>

          <Form.Item style={{ marginTop: 28, marginBottom: 12 }}>
            <Button
              type="primary"
              htmlType="submit"
              icon={<LoginOutlined />}
              loading={loading}
              block
              style={{
                height: 46,
                fontWeight: 600,
                fontSize: 15,
                background: 'linear-gradient(135deg, #F26F21 0%, #FA8C16 100%)',
                borderColor: '#F26F21',
              }}
            >
              Đăng nhập hệ thống
            </Button>
          </Form.Item>
        </Form>

        <div
          style={{
            marginTop: 24,
            padding: '12px 16px',
            background: '#FFF7E6',
            borderRadius: 8,
            border: '1px solid #FFE7BA',
          }}
        >
          <Text style={{ fontSize: 12, color: '#D46B08' }}>
            <strong>Lưu ý:</strong> Sinh viên vui lòng tra cứu thông tin trên ứng dụng F-School Mobile App.
          </Text>
        </div>
      </Card>

      {/* Warning Modal when STUDENT tries to log in */}
      <Modal
        title={
          <Space align="center">
            <div
              style={{
                width: 32,
                height: 32,
                borderRadius: '50%',
                background: '#FFF1F0',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#F5222D',
              }}
            >
              <MobileOutlined style={{ fontSize: 18 }} />
            </div>
            <span style={{ color: '#CF1322', fontWeight: 700, fontSize: 16 }}>
              Truy cập bị từ chối dành cho Sinh viên
            </span>
          </Space>
        }
        open={studentModalVisible}
        onOk={() => setStudentModalVisible(false)}
        onCancel={() => setStudentModalVisible(false)}
        footer={[
          <Button
            key="ok"
            type="primary"
            onClick={() => setStudentModalVisible(false)}
            style={{ background: '#F26F21', borderColor: '#F26F21' }}
          >
            Tôi đã hiểu
          </Button>,
        ]}
        width={500}
      >
        <div style={{ paddingTop: 12 }}>
          <Paragraph style={{ fontSize: 14, lineHeight: 1.6, color: '#262626' }}>
            Tài khoản của bạn là <strong>Sinh viên (STUDENT)</strong>.
          </Paragraph>
          <Paragraph style={{ fontSize: 14, lineHeight: 1.6, color: '#595959' }}>
            Cổng thông tin Web Portal F-School được thiết kế dành riêng cho <strong>Cán bộ Quản trị</strong> và{' '}
            <strong>Giảng viên</strong> để phục vụ công tác giảng dạy, điểm danh và quản trị học vụ.
          </Paragraph>

          <div
            style={{
              background: '#F6FFED',
              border: '1px solid #B7EB8F',
              borderRadius: 8,
              padding: 16,
              marginTop: 16,
            }}
          >
            <Text strong style={{ color: '#389E0D', display: 'block', marginBottom: 8 }}>
              <CheckCircleFilled /> Hướng dẫn dành cho Sinh viên:
            </Text>
            <ul style={{ margin: 0, paddingLeft: 20, color: '#595959', fontSize: 13 }}>
              <li>Tải ứng dụng <strong>F-School Mobile App</strong> trên App Store hoặc Google Play.</li>
              <li>Đăng nhập bằng tài khoản email sinh viên để xem lịch học cá nhân.</li>
              <li>Theo dõi kết quả điểm danh, bảng điểm và gửi đơn xin nghỉ/miễn điểm danh online.</li>
            </ul>
          </div>
        </div>
      </Modal>
    </div>
  )
}

export default LoginPage
