import React, { useState, useEffect } from 'react'
import { Outlet, useNavigate, useLocation, Link } from 'react-router-dom'
import {
  Layout,
  Menu,
  Button,
  Avatar,
  Dropdown,
  Tag,
  Badge,
  Modal,
  Form,
  Input,
  Breadcrumb,
  Space,
  Typography,
  message,
} from 'antd'
import type { MenuProps } from 'antd'
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  DashboardOutlined,
  TeamOutlined,
  BookOutlined,
  AppstoreOutlined,
  CalendarOutlined,
  FileDoneOutlined,
  UploadOutlined,
  NotificationOutlined,
  TrophyOutlined,
  ReadOutlined,
  HeartOutlined,
  BellOutlined,
  UserOutlined,
  KeyOutlined,
  LogoutOutlined,
  HomeOutlined,
} from '@ant-design/icons'
import { useAuth } from '@/contexts/AuthContext'
import apiClient from '@/api/client'
import ProfileModal from '@/components/ProfileModal'

const { Header, Sider, Content } = Layout
const { Title, Text } = Typography

export const MainLayout: React.FC = () => {
  const { user, role, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [collapsed, setCollapsed] = useState(false)
  const [unreadCount, setUnreadCount] = useState<number>(0)
  const [profileOpen, setProfileOpen] = useState(false)
  const [changePwdOpen, setChangePwdOpen] = useState(false)
  const [changePwdLoading, setChangePwdLoading] = useState(false)
  const [pwdForm] = Form.useForm()

  // Fetch unread notifications
  useEffect(() => {
    let isMounted = true
    const fetchUnread = async () => {
      try {
        const res = await apiClient.get<{ unreadCount: number }>('/notifications/unread-count')
        if (isMounted) {
          setUnreadCount(res.data.unreadCount || 0)
        }
      } catch {
        // Ignore failure
      }
    }

    void fetchUnread()
    const timer = setInterval(() => {
      void fetchUnread()
    }, 30000)
    return () => {
      isMounted = false
      clearInterval(timer)
    }
  }, [])

  // Dynamic Navigation Menu tailored to Role
  const adminMenuItems: MenuProps['items'] = [
    {
      key: '/admin/dashboard',
      icon: <DashboardOutlined />,
      label: 'Bàn làm việc (Dashboard)',
    },
    {
      key: '/admin/users',
      icon: <TeamOutlined />,
      label: 'Quản lý Người dùng',
    },
    {
      key: '/admin/courses',
      icon: <BookOutlined />,
      label: 'Quản lý Môn học',
    },
    {
      key: '/admin/classes',
      icon: <AppstoreOutlined />,
      label: 'Quản lý Lớp học phần',
    },
    {
      key: '/admin/schedules',
      icon: <CalendarOutlined />,
      label: 'Thời khóa biểu Trường',
    },
    {
      key: '/admin/requests',
      icon: <FileDoneOutlined />,
      label: 'Duyệt đơn từ sinh viên',
    },
    {
      key: '/admin/import',
      icon: <UploadOutlined />,
      label: 'Nạp dữ liệu CSV',
    },
    {
      key: '/admin/news',
      icon: <NotificationOutlined />,
      label: 'Quản trị Tin tức',
    },
    {
      type: 'divider',
    },
    {
      key: '/news',
      icon: <ReadOutlined />,
      label: 'Tin tức nhà trường',
    },
    {
      key: '/clubs',
      icon: <HeartOutlined />,
      label: 'Câu lạc bộ sinh viên',
    },
    {
      key: '/notifications',
      icon: <BellOutlined />,
      label: (
        <span>
          Trung tâm Thông báo{' '}
          {unreadCount > 0 && <Badge count={unreadCount} style={{ marginLeft: 8 }} />}
        </span>
      ),
    },
  ]

  const lecturerMenuItems: MenuProps['items'] = [
    {
      key: '/teacher/schedule',
      icon: <CalendarOutlined />,
      label: 'Lịch giảng dạy',
    },
    {
      key: '/teacher/grades',
      icon: <TrophyOutlined />,
      label: 'Bảng điểm lớp học',
    },
    {
      type: 'divider',
    },
    {
      key: '/news',
      icon: <ReadOutlined />,
      label: 'Tin tức nhà trường',
    },
    {
      key: '/clubs',
      icon: <HeartOutlined />,
      label: 'Câu lạc bộ sinh viên',
    },
    {
      key: '/notifications',
      icon: <BellOutlined />,
      label: (
        <span>
          Trung tâm Thông báo{' '}
          {unreadCount > 0 && <Badge count={unreadCount} style={{ marginLeft: 8 }} />}
        </span>
      ),
    },
  ]

  const menuItems = role === 'ADMIN' ? adminMenuItems : lecturerMenuItems

  // Breadcrumbs calculation
  const getBreadcrumbs = () => {
    const path = location.pathname
    const parts = [
      {
        title: (
          <Link to={role === 'ADMIN' ? '/admin/dashboard' : '/teacher/schedule'}>
            <HomeOutlined /> Trang chủ
          </Link>
        ),
      },
    ]

    const pathMap: Record<string, string> = {
      '/admin/dashboard': 'Dashboard Quản trị',
      '/admin/users': 'Quản lý Người dùng',
      '/admin/courses': 'Quản lý Môn học',
      '/admin/classes': 'Quản lý Lớp học phần',
      '/admin/schedules': 'Thời khóa biểu Toàn trường',
      '/admin/requests': 'Duyệt đơn từ sinh viên',
      '/admin/import': 'Nạp dữ liệu hàng loạt (CSV)',
      '/admin/news': 'Quản trị Tin tức',
      '/teacher/schedule': 'Lịch giảng dạy',
      '/teacher/attendance': 'Điểm danh sinh viên',
      '/teacher/grades': 'Bảng điểm lớp học',
      '/news': 'Tin tức nhà trường',
      '/clubs': 'Câu lạc bộ sinh viên',
      '/notifications': 'Trung tâm Thông báo',
    }

    if (path.startsWith('/news/') && path !== '/news') {
      parts.push({ title: <Link to="/news">Tin tức</Link> })
      parts.push({ title: <span>Chi tiết bài viết</span> })
    } else if (pathMap[path]) {
      parts.push({ title: <span>{pathMap[path]}</span> })
    }

    return parts
  }

  // Handle Change Password Form
  const handleChangePassword = async (values: { oldPassword: string; newPassword: string }) => {
    try {
      setChangePwdLoading(true)
      await apiClient.post('/auth/change-password', {
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
      })
      message.success('Đổi mật khẩu thành công! Vui lòng bảo mật mật khẩu mới.')
      setChangePwdOpen(false)
      pwdForm.resetFields()
    } catch (err: unknown) {
      const errDetail =
        (err as { rfcMessage?: string })?.rfcMessage || 'Đổi mật khẩu thất bại. Vui lòng kiểm tra lại mật khẩu cũ!'
      message.error(errDetail)
    } finally {
      setChangePwdLoading(false)
    }
  }

  const userDropdownItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Thông tin tài khoản',
      onClick: () => setProfileOpen(true),
    },
    {
      key: 'change-pwd',
      icon: <KeyOutlined />,
      label: 'Đổi mật khẩu',
      onClick: () => setChangePwdOpen(true),
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      danger: true,
      label: 'Đăng xuất',
      onClick: () => logout(),
    },
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* Sider Navigation */}
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        trigger={null}
        width={260}
        style={{
          background: '#001529',
          boxShadow: '2px 0 8px 0 rgba(29,35,41,.05)',
        }}
      >
        {/* Brand Header */}
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'flex-start',
            padding: collapsed ? '0' : '0 20px',
            background: '#002140',
            borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
          }}
        >
          <div
            style={{
              width: 36,
              height: 36,
              borderRadius: 8,
              background: 'linear-gradient(135deg, #F26F21 0%, #FA8C16 100%)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
              fontWeight: 800,
              fontSize: 18,
              marginRight: collapsed ? 0 : 12,
              flexShrink: 0,
            }}
          >
            F
          </div>
          {!collapsed && (
            <div style={{ overflow: 'hidden', whiteSpace: 'nowrap' }}>
              <Title
                level={4}
                style={{
                  color: '#fff',
                  margin: 0,
                  fontSize: 16,
                  fontWeight: 700,
                  letterSpacing: 0.5,
                }}
              >
                F-SCHOOL PORTAL
              </Title>
              <Text style={{ color: '#FA8C16', fontSize: 11, fontWeight: 500 }}>
                {role === 'ADMIN' ? 'HỆ THỐNG QUẢN TRỊ' : 'CỔNG GIẢNG VIÊN'}
              </Text>
            </div>
          )}
        </div>

        {/* Menu Items */}
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{
            background: '#001529',
            marginTop: 8,
            borderRight: 0,
          }}
        />
      </Sider>

      {/* Main Container */}
      <Layout>
        {/* Top Header */}
        <Header
          style={{
            padding: '0 24px',
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 1px 4px rgba(0, 21, 41, 0.08)',
            zIndex: 10,
          }}
        >
          {/* Left section: Collapse Trigger & Current Title */}
          <Space size="middle" align="center">
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              style={{ fontSize: '16px', width: 40, height: 40 }}
            />
            <Text strong style={{ fontSize: 15, color: '#002766' }}>
              {role === 'ADMIN' ? 'FPT Education Admin Workstation' : 'F-School Lecturer Hub'}
            </Text>
          </Space>

          {/* Right section: Notifications, Role Tag, User Menu */}
          <Space size="large" align="center">
            {/* Notification Bell */}
            <Badge count={unreadCount} offset={[-2, 2]}>
              <Button
                type="text"
                shape="circle"
                icon={<BellOutlined style={{ fontSize: 18, color: '#595959' }} />}
                onClick={() => navigate('/notifications')}
                title="Thông báo"
              />
            </Badge>

            {/* Role Tag */}
            {role === 'ADMIN' ? (
              <Tag color="volcano" style={{ fontWeight: 600, padding: '2px 10px' }}>
                ADMINISTRATOR
              </Tag>
            ) : (
              <Tag color="orange" style={{ fontWeight: 600, padding: '2px 10px' }}>
                GIẢNG VIÊN
              </Tag>
            )}

            {/* User Dropdown */}
            <Dropdown menu={{ items: userDropdownItems }} trigger={['click']} placement="bottomRight">
              <Space style={{ cursor: 'pointer', userSelect: 'none' }}>
                <Avatar
                  style={{
                    backgroundColor: role === 'ADMIN' ? '#002766' : '#F26F21',
                    verticalAlign: 'middle',
                  }}
                  icon={<UserOutlined />}
                  src={user?.avatarUrl}
                />
                <div style={{ display: 'flex', flexDirection: 'column', lineHeight: 1.2 }}>
                  <Text strong style={{ fontSize: 13 }}>
                    {user?.fullName || 'Người dùng'}
                  </Text>
                  <Text type="secondary" style={{ fontSize: 11 }}>
                    {user?.rollNumber || user?.email}
                  </Text>
                </div>
              </Space>
            </Dropdown>
          </Space>
        </Header>

        {/* Breadcrumb & Main Content Area */}
        <div style={{ padding: '16px 24px 0 24px' }}>
          <Breadcrumb items={getBreadcrumbs()} />
        </div>

        <Content
          style={{
            margin: '16px 24px 24px 24px',
            minHeight: 280,
            background: '#F0F2F5',
          }}
        >
          <Outlet />
        </Content>
      </Layout>

      {/* Profile Modal */}
      <ProfileModal open={profileOpen} onClose={() => setProfileOpen(false)} />

      {/* Change Password Modal */}
      <Modal
        title={
          <Space>
            <KeyOutlined style={{ color: '#F26F21' }} />
            <span>Đổi mật khẩu tài khoản</span>
          </Space>
        }
        open={changePwdOpen}
        onCancel={() => {
          setChangePwdOpen(false)
          pwdForm.resetFields()
        }}
        footer={null}
        destroyOnClose
      >
        <Form form={pwdForm} layout="vertical" onFinish={handleChangePassword}>
          <Form.Item
            name="oldPassword"
            label="Mật khẩu hiện tại"
            rules={[{ required: true, message: 'Vui lòng nhập mật khẩu hiện tại' }]}
          >
            <Input.Password placeholder="Nhập mật khẩu hiện tại" />
          </Form.Item>

          <Form.Item
            name="newPassword"
            label="Mật khẩu mới (8-12 ký tự, gồm hoa, thường, số, ký tự đặc biệt)"
            rules={[
              { required: true, message: 'Vui lòng nhập mật khẩu mới' },
              {
                pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,12}$/,
                message: 'Mật khẩu phải từ 8-12 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt',
              },
            ]}
          >
            <Input.Password placeholder="Nhập mật khẩu mới" />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="Xác nhận mật khẩu mới"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: 'Vui lòng xác nhận mật khẩu mới' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve()
                  }
                  return Promise.reject(new Error('Mật khẩu xác nhận không khớp!'))
                },
              }),
            ]}
          >
            <Input.Password placeholder="Nhập lại mật khẩu mới" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setChangePwdOpen(false)}>Hủy</Button>
              <Button type="primary" htmlType="submit" loading={changePwdLoading}>
                Cập nhật mật khẩu
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  )
}

export default MainLayout
