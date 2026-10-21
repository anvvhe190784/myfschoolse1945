import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Button, Typography, Tag, Space, Divider, Spin, Breadcrumb } from 'antd'
import {
  ArrowLeftOutlined,
  CalendarOutlined,
  EyeOutlined,
  UserOutlined,
  HomeOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import apiClient from '@/api/client'
import { NewsItem } from '@/types'

const { Title, Text, Paragraph } = Typography

export const NewsDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [news, setNews] = useState<NewsItem | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!id) return
    const fetchDetail = async () => {
      setLoading(true)
      try {
        const res = await apiClient.get<NewsItem>(`/news/${id}`)
        setNews(res.data)
      } catch {
        // Fallback or error handled
      } finally {
        setLoading(false)
      }
    }
    void fetchDetail()
  }, [id])

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Spin size="large" tip="Đang tải nội dung bài viết..." />
      </div>
    )
  }

  if (!news) {
    return (
      <Card style={{ borderRadius: 12, textAlign: 'center', padding: '60px 0' }}>
        <Title level={4}>Không tìm thấy bài viết</Title>
        <Button type="primary" onClick={() => { void navigate('/news') }}>
          Quay lại danh sách tin tức
        </Button>
      </Card>
    )
  }

  return (
    <div style={{ maxWidth: 900, margin: '0 auto' }}>
      <div style={{ marginBottom: 16 }}>
        <Breadcrumb
          items={[
            {
              title: (
                <Typography.Link onClick={() => { void navigate('/') }}>
                  <HomeOutlined /> Trang chủ
                </Typography.Link>
              ),
            },
            {
              title: (
                <Typography.Link onClick={() => { void navigate('/news') }}>
                  Tin tức
                </Typography.Link>
              ),
            },
            {
              title: <span>{news.title}</span>,
            },
          ]}
        />
      </div>

      <Card
        style={{
          borderRadius: 12,
          boxShadow: '0 2px 10px rgba(0,0,0,0.06)',
          border: '1px solid #f0f0f0',
        }}
        styles={{ body: { padding: '32px 40px' } }}
      >
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          onClick={() => { void navigate('/news') }}
          style={{ marginBottom: 16, paddingLeft: 0, color: '#F26F21', fontWeight: 600 }}
        >
          Quay lại danh sách tin tức
        </Button>

        <Tag color="orange" style={{ fontWeight: 600, marginBottom: 12 }}>
          {news.category || 'TIN TỨC CHUNG'}
        </Tag>

        <Title level={2} style={{ color: '#002766', marginTop: 8, marginBottom: 16 }}>
          {news.title}
        </Title>

        <Space size="large" wrap style={{ color: '#8c8c8c', fontSize: 13, marginBottom: 24 }}>
          <Space size="small">
            <UserOutlined />
            <span>Tác giả: <strong>{news.authorName || 'Ban Truyền Thông'}</strong></span>
          </Space>
          <Space size="small">
            <CalendarOutlined />
            <span>Đăng ngày: {dayjs(news.publishedAt).format('HH:mm - DD/MM/YYYY')}</span>
          </Space>
          <Space size="small">
            <EyeOutlined />
            <span>{news.viewsCount || 0} lượt xem</span>
          </Space>
        </Space>

        {news.thumbnailUrl && (
          <div style={{ marginBottom: 28, borderRadius: 10, overflow: 'hidden' }}>
            <img
              src={news.thumbnailUrl}
              alt={news.title}
              style={{ width: '100%', maxHeight: 420, objectFit: 'cover' }}
            />
          </div>
        )}

        <div
          style={{
            background: '#F9FAFB',
            borderLeft: '4px solid #F26F21',
            padding: '16px 20px',
            borderRadius: '0 8px 8px 0',
            marginBottom: 28,
          }}
        >
          <Paragraph
            style={{
              fontSize: 15,
              fontWeight: 500,
              color: '#374151',
              margin: 0,
              fontStyle: 'italic',
            }}
          >
            {news.summary}
          </Paragraph>
        </div>

        <Divider />

        <div
          style={{
            fontSize: 15,
            lineHeight: 1.8,
            color: '#1f2937',
            whiteSpace: 'pre-line',
          }}
        >
          {news.content}
        </div>
      </Card>
    </div>
  )
}

export default NewsDetailPage
