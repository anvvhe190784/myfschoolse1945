import React, { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
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
  Row,
  Col,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  NotificationOutlined,
  PlusOutlined,
  EyeOutlined,
  SearchOutlined,
  UserOutlined,
  CalendarOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import apiClient from '@/api/client'
import { NewsItem } from '@/types'

const { Title, Text } = Typography
const { Search } = Input

export const NewsManagementPage: React.FC = () => {
  const navigate = useNavigate()
  const [newsList, setNewsList] = useState<NewsItem[]>([])
  const [loading, setLoading] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL')

  // Create News Modal
  const [createModalOpen, setCreateModalOpen] = useState(false)
  const [createLoading, setCreateLoading] = useState(false)
  const [form] = Form.useForm()

  const fetchNews = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiClient.get<NewsItem[]>('/news', {
        params: { limit: 100 },
      })
      setNewsList(res.data || [])
    } catch {
      message.error('Không thể tải danh sách bài viết')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void fetchNews()
  }, [fetchNews])

  const handleCreateNews = async (values: {
    title: string
    category: string
    thumbnailUrl?: string
    summary: string
    content: string
  }) => {
    setCreateLoading(true)
    try {
      await apiClient.post('/news', values)
      message.success('Đăng bài viết và phát sóng thông báo thành công!')
      setCreateModalOpen(false)
      form.resetFields()
      void fetchNews()
    } catch (err: unknown) {
      const msg =
        (err as { rfcMessage?: string })?.rfcMessage || 'Không thể đăng bài viết'
      message.error(msg)
    } finally {
      setCreateLoading(false)
    }
  }

  const filteredNews = newsList.filter((item) => {
    const matchSearch =
      item.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.summary.toLowerCase().includes(searchTerm.toLowerCase())
    const matchCat = categoryFilter === 'ALL' || item.category === categoryFilter
    return matchSearch && matchCat
  })

  const columns: ColumnsType<NewsItem> = [
    {
      title: 'Ảnh đại diện',
      dataIndex: 'thumbnailUrl',
      key: 'thumbnailUrl',
      width: 100,
      render: (url: string) =>
        url ? (
          <img
            src={url}
            alt="Thumbnail"
            style={{ width: 80, height: 50, objectFit: 'cover', borderRadius: 6 }}
          />
        ) : (
          <div
            style={{
              width: 80,
              height: 50,
              borderRadius: 6,
              background: '#f0f0f0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 11,
              color: '#8c8c8c',
            }}
          >
            No image
          </div>
        ),
    },
    {
      title: 'Tiêu đề bài viết & Tóm tắt',
      key: 'title',
      render: (_, record) => (
        <div>
          <Text strong style={{ color: '#002766', fontSize: 14 }}>
            {record.title}
          </Text>
          <div style={{ fontSize: 12, color: '#595959', marginTop: 2 }}>{record.summary}</div>
        </div>
      ),
    },
    {
      title: 'Danh mục',
      dataIndex: 'category',
      key: 'category',
      width: 140,
      render: (cat: string) => {
        let color = 'orange'
        if (cat === 'ACADEMIC') color = 'blue'
        if (cat === 'EVENT') color = 'gold'
        if (cat === 'ACTIVITY') color = 'green'
        return <Tag color={color}>{cat || 'CHUNG'}</Tag>
      },
    },
    {
      title: 'Người đăng',
      dataIndex: 'authorName',
      key: 'authorName',
      width: 150,
      render: (author: string) => (
        <Space>
          <UserOutlined style={{ color: '#F26F21' }} />
          <span>{author || 'Ban Biên Tập'}</span>
        </Space>
      ),
    },
    {
      title: 'Thời gian đăng',
      dataIndex: 'publishedAt',
      key: 'publishedAt',
      width: 140,
      render: (date: string) => (
        <Space size="small">
          <CalendarOutlined style={{ color: '#8c8c8c' }} />
          <span style={{ fontSize: 12 }}>{dayjs(date).format('DD/MM/YYYY')}</span>
        </Space>
      ),
    },
    {
      title: 'Lượt xem',
      dataIndex: 'viewsCount',
      key: 'viewsCount',
      width: 100,
      align: 'center',
      render: (views: number) => <Tag color="default">{views || 0}</Tag>,
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 100,
      align: 'center',
      render: (_, record) => (
        <Button
          type="link"
          icon={<EyeOutlined />}
          onClick={() => navigate(`/news/${record.newsId}`)}
          style={{ color: '#F26F21', fontWeight: 600 }}
        >
          Xem
        </Button>
      ),
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
            <NotificationOutlined style={{ color: '#F26F21', marginRight: 10 }} />
            Quản Trị Tin Tức & Thông Báo
          </Title>
          <Text type="secondary">Đăng tải thông báo, sự kiện và tin tức hoạt động nhà trường</Text>
        </div>

        <Button
          type="primary"
          icon={<PlusOutlined />}
          size="large"
          onClick={() => setCreateModalOpen(true)}
          style={{ background: '#F26F21', borderColor: '#F26F21', fontWeight: 600 }}
        >
          Đăng bài viết mới
        </Button>
      </div>

      {/* Filter Toolbar */}
      <Card
        style={{ borderRadius: 12, marginBottom: 20, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}
        styles={{ body: { padding: '16px 20px' } }}
      >
        <Space wrap size="middle">
          <Search
            placeholder="Tìm theo tiêu đề, nội dung tóm tắt..."
            allowClear
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: 320 }}
            prefix={<SearchOutlined />}
          />

          <Select
            value={categoryFilter}
            onChange={setCategoryFilter}
            style={{ width: 180 }}
            options={[
              { value: 'ALL', label: 'Tất cả danh mục' },
              { value: 'ACADEMIC', label: 'Học vụ (ACADEMIC)' },
              { value: 'EVENT', label: 'Sự kiện (EVENT)' },
              { value: 'ACTIVITY', label: 'Hoạt động (ACTIVITY)' },
              { value: 'ANNOUNCEMENT', label: 'Thông báo chung' },
            ]}
          />
        </Space>
      </Card>

      {/* Table */}
      <Card style={{ borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
        <Table
          columns={columns}
          dataSource={filteredNews}
          rowKey="newsId"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>

      {/* Create News Modal */}
      <Modal
        title={
          <Space>
            <NotificationOutlined style={{ color: '#F26F21' }} />
            <span>Đăng bài viết & Phát sóng tin tức mới</span>
          </Space>
        }
        open={createModalOpen}
        onCancel={() => {
          setCreateModalOpen(false)
          form.resetFields()
        }}
        footer={null}
        destroyOnClose
        width={700}
      >
        <Form form={form} layout="vertical" onFinish={handleCreateNews}>
          <Form.Item
            name="title"
            label="Tiêu đề bài viết"
            rules={[{ required: true, message: 'Vui lòng nhập tiêu đề bài viết' }]}
          >
            <Input placeholder="Ví dụ: Thông báo lịch thi cuối kỳ học kỳ Fall 2024" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="category"
                label="Danh mục tin tức"
                initialValue="ACADEMIC"
                rules={[{ required: true, message: 'Chọn danh mục' }]}
              >
                <Select
                  options={[
                    { value: 'ACADEMIC', label: 'Học vụ (ACADEMIC)' },
                    { value: 'EVENT', label: 'Sự kiện (EVENT)' },
                    { value: 'ACTIVITY', label: 'Hoạt động SV (ACTIVITY)' },
                    { value: 'ANNOUNCEMENT', label: 'Thông báo chung (ANNOUNCEMENT)' },
                  ]}
                />
              </Form.Item>
            </Col>

            <Col span={12}>
              <Form.Item name="thumbnailUrl" label="Đường dẫn ảnh đại diện (Thumbnail URL)">
                <Input placeholder="https://example.com/image.jpg" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="summary"
            label="Tóm tắt ngắn (Summary)"
            rules={[{ required: true, message: 'Vui lòng nhập tóm tắt' }]}
          >
            <Input.TextArea rows={2} placeholder="Mô tả tóm tắt nội dung chính trong 1-2 câu..." />
          </Form.Item>

          <Form.Item
            name="content"
            label="Nội dung bài viết chi tiết"
            rules={[{ required: true, message: 'Vui lòng nhập nội dung chi tiết' }]}
          >
            <Input.TextArea rows={6} placeholder="Nhập toàn bộ nội dung thông báo, hướng dẫn, chi tiết..." />
          </Form.Item>

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setCreateModalOpen(false)}>Hủy</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={createLoading}
                style={{ background: '#F26F21', borderColor: '#F26F21' }}
              >
                Đăng bài & Phát sóng
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default NewsManagementPage
