import React, { useState, useEffect, useCallback } from 'react'
import {
  Card,
  Table,
  Button,
  Tag,
  Space,
  Typography,
  Input,
  Select,
  Modal,
  Form,
  Popconfirm,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  UserOutlined,
  UserAddOutlined,
  LockOutlined,
  UnlockOutlined,
  SearchOutlined,
} from '@ant-design/icons'
import apiClient from '@/api/client'
import { User, UserRole } from '@/types'

const { Title, Text } = Typography
const { Search } = Input

export const UserManagementPage: React.FC = () => {
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedRole, setSelectedRole] = useState<string>('ALL')
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL')

  // Add User Modal
  const [addModalOpen, setAddModalOpen] = useState(false)
  const [addLoading, setAddLoading] = useState(false)
  const [form] = Form.useForm()

  const fetchUsers = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiClient.get<User[]>('/admin/users', {
        params: {
          page,
          size: pageSize,
        },
      })
      setUsers(res.data || [])
    } catch {
      message.error('Không thể tải danh sách người dùng')
    } finally {
      setLoading(false)
    }
  }, [page, pageSize])

  useEffect(() => {
    void fetchUsers()
  }, [fetchUsers])

  // Filter users locally based on search, role, status
  const filteredUsers = users.filter((u) => {
    const matchSearch =
      !searchTerm ||
      u.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      u.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (u.rollNumber && u.rollNumber.toLowerCase().includes(searchTerm.toLowerCase()))

    const matchRole = selectedRole === 'ALL' || u.role === selectedRole
    const matchStatus = selectedStatus === 'ALL' || u.status === selectedStatus

    return matchSearch && matchRole && matchStatus
  })

  // Handle Add User
  const handleAddUser = async (values: {
    rollNumber?: string
    email: string
    password: string
    fullName: string
    phoneNumber?: string
    role: UserRole
  }) => {
    setAddLoading(true)
    try {
      await apiClient.post('/admin/users', values)
      message.success(`Tạo người dùng ${values.fullName} thành công!`)
      setAddModalOpen(false)
      form.resetFields()
      void fetchUsers()
    } catch (err: unknown) {
      const msg =
        (err as { rfcMessage?: string })?.rfcMessage || 'Tạo người dùng thất bại. Vui lòng kiểm tra lại thông tin!'
      message.error(msg)
    } finally {
      setAddLoading(false)
    }
  }

  // Handle Lock/Unlock Status
  const handleToggleStatus = async (record: User) => {
    const nextStatus = record.status === 'LOCKED' ? 'ACTIVE' : 'LOCKED'
    try {
      await apiClient.patch(`/admin/users/${record.userId}/status`, {
        status: nextStatus,
      })
      message.success(`Đã chuyển trạng thái người dùng sang ${nextStatus}!`)
      setUsers((prev) =>
        prev.map((u) => (u.userId === record.userId ? { ...u, status: nextStatus } : u))
      )
    } catch (err: unknown) {
      const msg =
        (err as { rfcMessage?: string })?.rfcMessage || 'Không thể thay đổi trạng thái người dùng'
      message.error(msg)
    }
  }

  const columns: ColumnsType<User> = [
    {
      title: 'Mã số / RollNo',
      dataIndex: 'rollNumber',
      key: 'rollNumber',
      width: 140,
      render: (roll: string) => (
        <Text strong style={{ color: '#002766', fontFamily: 'monospace' }}>
          {roll || '—'}
        </Text>
      ),
    },
    {
      title: 'Họ và tên',
      dataIndex: 'fullName',
      key: 'fullName',
      render: (name: string, record) => (
        <Space>
          <UserOutlined style={{ color: '#F26F21' }} />
          <div>
            <Text strong>{name}</Text>
            <div style={{ fontSize: 11, color: '#8c8c8c' }}>{record.email}</div>
          </div>
        </Space>
      ),
    },
    {
      title: 'Số điện thoại',
      dataIndex: 'phoneNumber',
      key: 'phoneNumber',
      render: (phone: string) => <Text type="secondary">{phone || '—'}</Text>,
    },
    {
      title: 'Vai trò (Role)',
      dataIndex: 'role',
      key: 'role',
      width: 140,
      render: (role: string) => {
        switch (role) {
          case 'ADMIN':
            return <Tag color="volcano" style={{ fontWeight: 600 }}>ADMIN</Tag>
          case 'LECTURER':
            return <Tag color="orange" style={{ fontWeight: 600 }}>GIẢNG VIÊN</Tag>
          case 'STUDENT':
            return <Tag color="blue" style={{ fontWeight: 600 }}>SINH VIÊN</Tag>
          case 'PARENT':
            return <Tag color="purple" style={{ fontWeight: 600 }}>PHỤ HUYNH</Tag>
          default:
            return <Tag>{role}</Tag>
        }
      },
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 130,
      render: (status?: string) => {
        if (status === 'LOCKED') return <Tag color="error">ĐÃ KHÓA</Tag>
        if (status === 'INACTIVE') return <Tag color="default">CHƯA KÍCH HOẠT</Tag>
        return <Tag color="success">HOẠT ĐỘNG</Tag>
      },
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 140,
      align: 'center',
      render: (_, record) => {
        const isLocked = record.status === 'LOCKED'
        return (
          <Popconfirm
            title={isLocked ? 'Mở khóa tài khoản' : 'Khóa tài khoản'}
            description={`Bạn có chắc muốn ${isLocked ? 'mở khóa' : 'khóa'} tài khoản ${record.fullName}?`}
            onConfirm={() => handleToggleStatus(record)}
            okText="Đồng ý"
            cancelText="Hủy"
          >
            <Button
              type={isLocked ? 'primary' : 'default'}
              danger={!isLocked}
              size="small"
              icon={isLocked ? <UnlockOutlined /> : <LockOutlined />}
            >
              {isLocked ? 'Mở khóa' : 'Khóa'}
            </Button>
          </Popconfirm>
        )
      },
    },
  ]

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      {/* Page Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 20,
          flexWrap: 'wrap',
          gap: 16,
        }}
      >
        <div>
          <Title level={3} style={{ margin: 0, color: '#002766' }}>
            <UserOutlined style={{ color: '#F26F21', marginRight: 10 }} />
            Quản Lý Người Dùng
          </Title>
          <Text type="secondary">Quản lý danh sách sinh viên, giảng viên và cán bộ quản trị</Text>
        </div>

        <Button
          type="primary"
          icon={<UserAddOutlined />}
          size="large"
          onClick={() => setAddModalOpen(true)}
          style={{ background: '#F26F21', borderColor: '#F26F21', fontWeight: 600 }}
        >
          Thêm người dùng mới
        </Button>
      </div>

      {/* Filter Toolbar */}
      <Card
        style={{ borderRadius: 12, marginBottom: 20, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}
        styles={{ body: { padding: '16px 20px' } }}
      >
        <Space wrap size="middle">
          <Search
            placeholder="Tìm theo tên, email, MSSV..."
            allowClear
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: 280 }}
            prefix={<SearchOutlined />}
          />

          <Select
            value={selectedRole}
            onChange={setSelectedRole}
            style={{ width: 180 }}
            options={[
              { value: 'ALL', label: 'Tất cả vai trò' },
              { value: 'STUDENT', label: 'Sinh viên (Student)' },
              { value: 'LECTURER', label: 'Giảng viên (Lecturer)' },
              { value: 'PARENT', label: 'Phụ huynh (Parent)' },
              { value: 'ADMIN', label: 'Quản trị viên (Admin)' },
            ]}
          />

          <Select
            value={selectedStatus}
            onChange={setSelectedStatus}
            style={{ width: 170 }}
            options={[
              { value: 'ALL', label: 'Tất cả trạng thái' },
              { value: 'ACTIVE', label: 'Đang hoạt động' },
              { value: 'LOCKED', label: 'Đã bị khóa' },
              { value: 'INACTIVE', label: 'Chưa kích hoạt' },
            ]}
          />
        </Space>
      </Card>

      {/* Users Table */}
      <Card style={{ borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
        <Table
          columns={columns}
          dataSource={filteredUsers}
          rowKey="userId"
          loading={loading}
          pagination={{
            current: page + 1,
            pageSize,
            total: filteredUsers.length,
            onChange: (p, ps) => {
              setPage(p - 1)
              setPageSize(ps)
            },
            showSizeChanger: true,
          }}
        />
      </Card>

      {/* Add User Modal */}
      <Modal
        title={
          <Space>
            <UserAddOutlined style={{ color: '#F26F21' }} />
            <span>Thêm tài khoản người dùng mới</span>
          </Space>
        }
        open={addModalOpen}
        onCancel={() => {
          setAddModalOpen(false)
          form.resetFields()
        }}
        footer={null}
        destroyOnClose
      >
        <Form form={form} layout="vertical" onFinish={handleAddUser}>
          <Form.Item
            name="fullName"
            label="Họ và tên"
            rules={[{ required: true, message: 'Vui lòng nhập họ và tên' }]}
          >
            <Input placeholder="Ví dụ: Nguyễn Văn An" />
          </Form.Item>

          <Form.Item
            name="email"
            label="Địa chỉ Email"
            rules={[
              { required: true, message: 'Vui lòng nhập email' },
              { type: 'email', message: 'Email không đúng định dạng' },
            ]}
          >
            <Input placeholder="Ví dụ: annv@fpt.edu.vn" />
          </Form.Item>

          <Form.Item
            name="password"
            label="Mật khẩu khởi tạo"
            rules={[
              { required: true, message: 'Vui lòng nhập mật khẩu' },
              { min: 6, max: 50, message: 'Mật khẩu phải từ 6 đến 50 ký tự' },
            ]}
          >
            <Input.Password placeholder="Mật khẩu đăng nhập ban đầu" />
          </Form.Item>

          <Form.Item
            name="role"
            label="Vai trò (Role)"
            rules={[{ required: true, message: 'Vui lòng chọn vai trò' }]}
          >
            <Select
              placeholder="Chọn vai trò người dùng"
              options={[
                { value: 'STUDENT', label: 'Sinh viên (STUDENT)' },
                { value: 'LECTURER', label: 'Giảng viên (LECTURER)' },
                { value: 'PARENT', label: 'Phụ huynh (PARENT)' },
                { value: 'ADMIN', label: 'Quản trị viên (ADMIN)' },
              ]}
            />
          </Form.Item>

          <Form.Item name="rollNumber" label="Mã số sinh viên / Mã nhân viên (Roll Number)">
            <Input placeholder="Ví dụ: SE194501 hoặc GV001" />
          </Form.Item>

          <Form.Item name="phoneNumber" label="Số điện thoại liên hệ">
            <Input placeholder="Ví dụ: 0912345678" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setAddModalOpen(false)}>Hủy</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={addLoading}
                style={{ background: '#F26F21', borderColor: '#F26F21' }}
              >
                Tạo người dùng
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default UserManagementPage
