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
  InputNumber,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  AppstoreOutlined,
  PlusOutlined,
  SearchOutlined,
  UserOutlined,
  BookOutlined,
} from '@ant-design/icons'
import apiClient from '@/api/client'
import { ClassItem, Course, User } from '@/types'

const { Title, Text } = Typography
const { Search } = Input

export const ClassManagementPage: React.FC = () => {
  const [classes, setClasses] = useState<ClassItem[]>([])
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedSemester, setSelectedSemester] = useState<string>('ALL')

  // Available courses and lecturers for Open Class Form
  const [courses, setCourses] = useState<Course[]>([])
  const [lecturers, setLecturers] = useState<User[]>([])

  // Open Class Modal
  const [openClassModalVisible, setOpenClassModalVisible] = useState(false)
  const [createLoading, setCreateLoading] = useState(false)
  const [classForm] = Form.useForm()

  // Student Lookup Modal
  const [lookupModalVisible, setLookupModalVisible] = useState(false)
  const [studentIdInput, setStudentIdInput] = useState<string>('')
  const [studentClasses, setStudentClasses] = useState<ClassItem[]>([])
  const [lookupLoading, setLookupLoading] = useState(false)

  const fetchClasses = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiClient.get<ClassItem[]>('/admin/academic/classes', {
        params: { page, size: pageSize },
      })
      setClasses(res.data || [])
    } catch {
      message.error('Không thể tải danh sách lớp học phần')
    } finally {
      setLoading(false)
    }
  }, [page, pageSize])

  useEffect(() => {
    void fetchClasses()
  }, [fetchClasses])

  // Fetch courses and lecturers for modal dropdowns
  const fetchDropdownData = async () => {
    try {
      const [cRes, uRes] = await Promise.all([
        apiClient.get<Course[]>('/admin/academic/courses', { params: { size: 50 } }),
        apiClient.get<User[]>('/admin/users', { params: { size: 50 } }),
      ])
      setCourses(cRes.data || [])
      setLecturers((uRes.data || []).filter((u) => u.role === 'LECTURER'))
    } catch {
      // Ignore
    }
  }

  const handleOpenCreateModal = () => {
    void fetchDropdownData()
    setOpenClassModalVisible(true)
  }

  const handleCreateClass = async (values: {
    classCode: string
    courseId: number
    lecturerId?: number
    semester: string
    academicYear: number
    roomDefault?: string
  }) => {
    setCreateLoading(true)
    try {
      await apiClient.post('/admin/academic/classes', values)
      message.success(`Mở lớp học phần ${values.classCode} thành công!`)
      setOpenClassModalVisible(false)
      classForm.resetFields()
      void fetchClasses()
    } catch (err: unknown) {
      const msg =
        (err as { rfcMessage?: string })?.rfcMessage || 'Không thể tạo lớp học phần'
      message.error(msg)
    } finally {
      setCreateLoading(false)
    }
  }

  // Handle Lookup student enrolled classes
  const handleLookupStudent = async () => {
    if (!studentIdInput.trim()) {
      message.warning('Vui lòng nhập ID sinh viên')
      return
    }
    setLookupLoading(true)
    try {
      const res = await apiClient.get<ClassItem[]>(
        `/admin/academic/students/${studentIdInput.trim()}/classes`
      )
      setStudentClasses(res.data || [])
      if (!res.data || res.data.length === 0) {
        message.info('Sinh viên chưa đăng ký lớp học phần nào')
      }
    } catch (err: unknown) {
      const msg =
        (err as { rfcMessage?: string })?.rfcMessage || 'Tra cứu thất bại. Kiểm tra lại ID sinh viên!'
      message.error(msg)
      setStudentClasses([])
    } finally {
      setLookupLoading(false)
    }
  }

  const filteredClasses = classes.filter((c) => {
    const matchSearch =
      c.classCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.courseName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.courseCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (c.lecturerName && c.lecturerName.toLowerCase().includes(searchTerm.toLowerCase()))

    const matchSem = selectedSemester === 'ALL' || c.semester === selectedSemester
    return matchSearch && matchSem
  })

  const columns: ColumnsType<ClassItem> = [
    {
      title: 'Mã lớp (Class Code)',
      dataIndex: 'classCode',
      key: 'classCode',
      width: 150,
      render: (code: string) => (
        <Tag color="volcano" style={{ fontWeight: 700, fontSize: 13, padding: '2px 8px' }}>
          {code}
        </Tag>
      ),
    },
    {
      title: 'Môn học',
      key: 'course',
      render: (_, record) => (
        <div>
          <Text strong style={{ color: '#002766' }}>{record.courseName}</Text>
          <div style={{ fontSize: 12, color: '#595959' }}>{record.courseCode}</div>
        </div>
      ),
    },
    {
      title: 'Giảng viên phụ trách',
      dataIndex: 'lecturerName',
      key: 'lecturerName',
      render: (name: string) => (
        <Space>
          <UserOutlined style={{ color: '#F26F21' }} />
          <span>{name || 'Chưa phân công'}</span>
        </Space>
      ),
    },
    {
      title: 'Học kỳ & Năm',
      key: 'semester',
      width: 140,
      render: (_, record) => (
        <Space orientation="vertical" size={2}>
          <Tag color="blue">{record.semester}</Tag>
          <Text type="secondary" style={{ fontSize: 11 }}>Năm học {record.academicYear}</Text>
        </Space>
      ),
    },
    {
      title: 'Phòng mặc định',
      dataIndex: 'roomDefault',
      key: 'roomDefault',
      width: 130,
      render: (room: string) => <Tag color="cyan">{room || '—'}</Tag>,
    },
  ]

  const lookupColumns: ColumnsType<ClassItem> = [
    {
      title: 'Mã lớp',
      dataIndex: 'classCode',
      key: 'classCode',
      render: (c: string) => <Text strong>{c}</Text>,
    },
    {
      title: 'Môn học',
      dataIndex: 'courseName',
      key: 'courseName',
    },
    {
      title: 'Giảng viên',
      dataIndex: 'lecturerName',
      key: 'lecturerName',
    },
    {
      title: 'Học kỳ',
      dataIndex: 'semester',
      key: 'semester',
      render: (s: string) => <Tag color="blue">{s}</Tag>,
    },
  ]

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      {/* Header */}
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
            <AppstoreOutlined style={{ color: '#F26F21', marginRight: 10 }} />
            Quản Lý Lớp Học Phần
          </Title>
          <Text type="secondary">Quản lý lớp học theo học kỳ, phân công giảng viên và tra cứu đăng ký</Text>
        </div>

        <Space>
          <Button
            icon={<SearchOutlined />}
            size="large"
            onClick={() => setLookupModalVisible(true)}
          >
            Tra cứu lớp SV
          </Button>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            size="large"
            onClick={handleOpenCreateModal}
            style={{ background: '#F26F21', borderColor: '#F26F21', fontWeight: 600 }}
          >
            Mở lớp học phần mới
          </Button>
        </Space>
      </div>

      {/* Filter toolbar */}
      <Card
        style={{ borderRadius: 12, marginBottom: 20, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}
        styles={{ body: { padding: '16px 20px' } }}
      >
        <Space wrap size="middle">
          <Search
            placeholder="Tìm theo mã lớp, tên môn học, giảng viên..."
            allowClear
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: 320 }}
            prefix={<SearchOutlined />}
          />

          <Select
            value={selectedSemester}
            onChange={setSelectedSemester}
            style={{ width: 170 }}
            options={[
              { value: 'ALL', label: 'Tất cả học kỳ' },
              { value: 'FA24', label: 'Fall 2024 (FA24)' },
              { value: 'SP25', label: 'Spring 2025 (SP25)' },
              { value: 'SU25', label: 'Summer 2025 (SU25)' },
            ]}
          />
        </Space>
      </Card>

      {/* Main Table */}
      <Card style={{ borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
        <Table
          columns={columns}
          dataSource={filteredClasses}
          rowKey="classId"
          loading={loading}
          pagination={{
            current: page + 1,
            pageSize,
            total: filteredClasses.length,
            onChange: (p, ps) => {
              setPage(p - 1)
              setPageSize(ps)
            },
            showSizeChanger: true,
          }}
        />
      </Card>

      {/* Open Class Modal */}
      <Modal
        title={
          <Space>
            <AppstoreOutlined style={{ color: '#F26F21' }} />
            <span>Mở lớp học phần mới</span>
          </Space>
        }
        open={openClassModalVisible}
        onCancel={() => {
          setOpenClassModalVisible(false)
          classForm.resetFields()
        }}
        footer={null}
        destroyOnClose
      >
        <Form form={classForm} layout="vertical" onFinish={handleCreateClass}>
          <Form.Item
            name="classCode"
            label="Mã lớp học phần"
            rules={[{ required: true, message: 'Vui lòng nhập mã lớp' }]}
          >
            <Input placeholder="Ví dụ: SE1945-NJ, IA1802-HN" />
          </Form.Item>

          <Form.Item
            name="courseId"
            label="Môn học đào tạo"
            rules={[{ required: true, message: 'Vui lòng chọn môn học' }]}
          >
            <Select
              placeholder="Chọn môn học"
              options={courses.map((c) => ({
                value: c.courseId,
                label: `${c.courseCode} - ${c.courseName}`,
              }))}
            />
          </Form.Item>

          <Form.Item name="lecturerId" label="Giảng viên phụ trách">
            <Select
              placeholder="Chọn giảng viên phân công (tùy chọn)"
              allowClear
              options={lecturers.map((l) => ({
                value: l.userId,
                label: `${l.fullName} (${l.email})`,
              }))}
            />
          </Form.Item>

          <Form.Item
            name="semester"
            label="Học kỳ (Semester)"
            rules={[{ required: true, message: 'Vui lòng chọn hoặc nhập học kỳ' }]}
            initialValue="FA24"
          >
            <Select
              options={[
                { value: 'FA24', label: 'Fall 2024 (FA24)' },
                { value: 'SP25', label: 'Spring 2025 (SP25)' },
                { value: 'SU25', label: 'Summer 2025 (SU25)' },
              ]}
            />
          </Form.Item>

          <Form.Item
            name="academicYear"
            label="Năm học"
            initialValue={2024}
            rules={[{ required: true, message: 'Vui lòng nhập năm học' }]}
          >
            <InputNumber min={2020} max={2030} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="roomDefault" label="Phòng học mặc định">
            <Input placeholder="Ví dụ: P.302, Lab 4, Hội trường A" />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setOpenClassModalVisible(false)}>Hủy</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={createLoading}
                style={{ background: '#F26F21', borderColor: '#F26F21' }}
              >
                Tạo lớp học phần
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* Student Lookup Modal */}
      <Modal
        title={
          <Space>
            <UserOutlined style={{ color: '#F26F21' }} />
            <span>Tra cứu lớp học phần của sinh viên</span>
          </Space>
        }
        open={lookupModalVisible}
        onCancel={() => setLookupModalVisible(false)}
        footer={null}
        width={700}
      >
        <Space.Compact style={{ width: '100%', marginBottom: 16 }}>
          <Input
            placeholder="Nhập ID sinh viên (ví dụ: 1, 2, 3...)"
            value={studentIdInput}
            onChange={(e) => setStudentIdInput(e.target.value)}
            onPressEnter={handleLookupStudent}
          />
          <Button type="primary" onClick={handleLookupStudent} loading={lookupLoading} style={{ background: '#F26F21', borderColor: '#F26F21' }}>
            Tìm kiếm
          </Button>
        </Space.Compact>

        <Table
          columns={lookupColumns}
          dataSource={studentClasses}
          rowKey="classId"
          size="small"
          pagination={false}
          locale={{ emptyText: 'Chưa có dữ liệu lớp học phần của sinh viên' }}
        />
      </Modal>
    </div>
  )
}

export default ClassManagementPage
