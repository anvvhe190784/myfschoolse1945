import React, { useState, useEffect, useCallback } from 'react'
import {
  Card,
  Table,
  Button,
  Tag,
  Space,
  Typography,
  Input,
  Modal,
  Form,
  InputNumber,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  BookOutlined,
  PlusOutlined,
  SearchOutlined,
} from '@ant-design/icons'
import apiClient from '@/api/client'
import { Course } from '@/types'

const { Title, Text } = Typography
const { Search } = Input

export const CourseManagementPage: React.FC = () => {
  const [courses, setCourses] = useState<Course[]>([])
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [searchTerm, setSearchTerm] = useState('')

  // Add Course Modal
  const [addModalOpen, setAddModalOpen] = useState(false)
  const [addLoading, setAddLoading] = useState(false)
  const [form] = Form.useForm()

  const fetchCourses = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiClient.get<Course[]>('/admin/academic/courses', {
        params: { page, size: pageSize },
      })
      setCourses(res.data || [])
    } catch {
      message.error('Không thể tải danh sách môn học')
    } finally {
      setLoading(false)
    }
  }, [page, pageSize])

  useEffect(() => {
    void fetchCourses()
  }, [fetchCourses])

  const filteredCourses = courses.filter(
    (c) =>
      c.courseCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.courseName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (c.description && c.description.toLowerCase().includes(searchTerm.toLowerCase()))
  )

  const handleAddCourse = async (values: {
    courseCode: string
    courseName: string
    credits: number
    description?: string
  }) => {
    setAddLoading(true)
    try {
      await apiClient.post('/admin/academic/courses', values)
      message.success(`Thêm môn học ${values.courseCode} thành công!`)
      setAddModalOpen(false)
      form.resetFields()
      void fetchCourses()
    } catch (err: unknown) {
      const msg =
        (err as { rfcMessage?: string })?.rfcMessage || 'Thêm môn học thất bại'
      message.error(msg)
    } finally {
      setAddLoading(false)
    }
  }

  const columns: ColumnsType<Course> = [
    {
      title: 'Mã môn học',
      dataIndex: 'courseCode',
      key: 'courseCode',
      width: 140,
      render: (code: string) => (
        <Tag color="orange" style={{ fontWeight: 700, fontSize: 13, padding: '2px 8px' }}>
          {code}
        </Tag>
      ),
    },
    {
      title: 'Tên môn học',
      dataIndex: 'courseName',
      key: 'courseName',
      render: (name: string) => <Text strong style={{ color: '#002766', fontSize: 14 }}>{name}</Text>,
    },
    {
      title: 'Số tín chỉ',
      dataIndex: 'credits',
      key: 'credits',
      width: 120,
      align: 'center',
      render: (credits: number) => <Tag color="blue">{credits || 3} Tín chỉ</Tag>,
    },
    {
      title: 'Mô tả chi tiết',
      dataIndex: 'description',
      key: 'description',
      render: (desc: string) => <Text type="secondary">{desc || '—'}</Text>,
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
            <BookOutlined style={{ color: '#F26F21', marginRight: 10 }} />
            Quản Lý Môn Học
          </Title>
          <Text type="secondary">Danh mục chương trình đào tạo và học phần giảng dạy tại F-School</Text>
        </div>

        <Button
          type="primary"
          icon={<PlusOutlined />}
          size="large"
          onClick={() => setAddModalOpen(true)}
          style={{ background: '#F26F21', borderColor: '#F26F21', fontWeight: 600 }}
        >
          Thêm môn học mới
        </Button>
      </div>

      {/* Toolbar */}
      <Card
        style={{ borderRadius: 12, marginBottom: 20, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}
        styles={{ body: { padding: '16px 20px' } }}
      >
        <Search
          placeholder="Tìm theo mã môn, tên môn học..."
          allowClear
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ width: 320 }}
          prefix={<SearchOutlined />}
        />
      </Card>

      {/* Table */}
      <Card style={{ borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
        <Table
          columns={columns}
          dataSource={filteredCourses}
          rowKey="courseId"
          loading={loading}
          pagination={{
            current: page + 1,
            pageSize,
            total: filteredCourses.length,
            onChange: (p, ps) => {
              setPage(p - 1)
              setPageSize(ps)
            },
            showSizeChanger: true,
          }}
        />
      </Card>

      {/* Add Course Modal */}
      <Modal
        title={
          <Space>
            <BookOutlined style={{ color: '#F26F21' }} />
            <span>Thêm môn học mới vào hệ thống</span>
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
        <Form form={form} layout="vertical" onFinish={handleAddCourse}>
          <Form.Item
            name="courseCode"
            label="Mã môn học (Course Code)"
            rules={[{ required: true, message: 'Vui lòng nhập mã môn học' }]}
          >
            <Input placeholder="Ví dụ: PRJ301, SWP391, IOT102" />
          </Form.Item>

          <Form.Item
            name="courseName"
            label="Tên môn học"
            rules={[{ required: true, message: 'Vui lòng nhập tên môn học' }]}
          >
            <Input placeholder="Ví dụ: Java Web Application Development" />
          </Form.Item>

          <Form.Item
            name="credits"
            label="Số tín chỉ"
            initialValue={3}
            rules={[{ required: true, message: 'Vui lòng nhập số tín chỉ' }]}
          >
            <InputNumber min={1} max={10} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="description" label="Mô tả môn học">
            <Input.TextArea rows={3} placeholder="Nội dung tóm tắt, mục tiêu môn học..." />
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
                Lưu môn học
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default CourseManagementPage
