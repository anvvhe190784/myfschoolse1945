import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Card,
  Row,
  Col,
  Input,
  Select,
  Tag,
  Typography,
  Space,
  Empty,
  Spin,
  Button,
} from 'antd'
import {
  SearchOutlined,
  EyeOutlined,
  CalendarOutlined,
  UserOutlined,
  ReadOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import apiClient from '@/api/client'
import { NewsItem } from '@/types'

const { Title, Text, Paragraph } = Typography
const { Search } = Input

export const NewsPage: React.FC = () => {
  const navigate = useNavigate()
  const [newsList, setNewsList] = useState<NewsItem[]>([])
  const [loading, setLoading] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL')

  useEffect(() => {
    const fetchNews = async () => {
      setLoading(true)
      try {
        const res = await apiClient.get<NewsItem[]>('/news', {
          params: { limit: 50 },
        })
        setNewsList(res.data || [])
      } catch {
        // Fallback
      } finally {
        setLoading(false)
      }
    }
    void fetchNews()
  }, [])

  const filteredNews = newsList.filter((item) => {
    const matchSearch =
      item.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.summary.toLowerCase().includes(searchTerm.toLowerCase())
    const matchCategory =
      selectedCategory === 'ALL' || item.category === selectedCategory
    return matchSearch && matchCategory
  })

  const getCategoryColor = (cat?: string) => {
    switch (cat) {
      case 'ACADEMIC':
        return 'blue'
      case 'EVENT':
        return 'gold'
      case 'ACTIVITY':
        return 'green'
      case 'ANNOUNCEMENT':
        return 'volcano'
      default:
        return 'orange'
    }
  }

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      {/* Header bar */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 24,
          flexWrap: 'wrap',
          gap: 16,
        }}
      >
        <div>
          <Title level={3} style={{ margin: 0, color: '#002766' }}>
            <ReadOutlined style={{ color: '#F26F21', marginRight: 10 }} />
            Tin Tức & Thông Báo Nhà Trường
          </Title>
          <Text type="secondary">Cập nhật tin tức sự kiện, học vụ và đời sống học đường F-School</Text>
        </div>

        <Space size="middle">
          <Search
            placeholder="Tìm kiếm bài viết..."
            allowClear
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: 260 }}
            prefix={<SearchOutlined />}
          />
          <Select
            value={selectedCategory}
            onChange={setSelectedCategory}
            style={{ width: 160 }}
            options={[
              { value: 'ALL', label: 'Tất cả danh mục' },
              { value: 'ACADEMIC', label: 'Học vụ (Academic)' },
              { value: 'EVENT', label: 'Sự kiện (Event)' },
              { value: 'ACTIVITY', label: 'Hoạt động SV' },
              { value: 'ANNOUNCEMENT', label: 'Thông báo chung' },
            ]}
          />
        </Space>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '80px 0' }}>
          <Spin size="large" tip="Đang tải tin tức F-School..." />
        </div>
      ) : filteredNews.length === 0 ? (
        <Card style={{ borderRadius: 12, textAlign: 'center', padding: '40px 0' }}>
          <Empty description="Không tìm thấy bài viết nào phù hợp" />
        </Card>
      ) : (
        <Row gutter={[24, 24]}>
          {filteredNews.map((item) => (
            <Col xs={24} sm={12} lg={8} key={item.newsId}>
              <Card
                hoverable
                style={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  borderRadius: 12,
                  overflow: 'hidden',
                  boxShadow: '0 2px 10px rgba(0,0,0,0.06)',
                  border: '1px solid #f0f0f0',
                }}
                cover={
                  <div
                    style={{
                      height: 180,
                      position: 'relative',
                      background: item.thumbnailUrl
                        ? `url(${item.thumbnailUrl}) center/cover no-repeat`
                        : 'linear-gradient(135deg, #002766 0%, #003a8c 100%)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    {!item.thumbnailUrl && (
                      <ReadOutlined style={{ fontSize: 48, color: 'rgba(255,255,255,0.4)' }} />
                    )}
                    <div style={{ position: 'absolute', top: 12, left: 12 }}>
                      <Tag color={getCategoryColor(item.category)} style={{ fontWeight: 600 }}>
                        {item.category || 'CHUNG'}
                      </Tag>
                    </div>
                  </div>
                }
                styles={{
                  body: {
                    flex: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    padding: 20,
                  },
                }}
                onClick={() => { void navigate(`/news/${item.newsId}`) }}
              >
                <Title
                  level={5}
                  ellipsis={{ rows: 2 }}
                  style={{
                    margin: '0 0 10px 0',
                    color: '#002766',
                    minHeight: 44,
                    fontSize: 16,
                  }}
                >
                  {item.title}
                </Title>

                <Paragraph
                  ellipsis={{ rows: 3 }}
                  type="secondary"
                  style={{ fontSize: 13, flex: 1, marginBottom: 16 }}
                >
                  {item.summary}
                </Paragraph>

                <div
                  style={{
                    borderTop: '1px solid #f5f5f5',
                    paddingTop: 12,
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    fontSize: 12,
                    color: '#8c8c8c',
                  }}
                >
                  <Space size="small">
                    <UserOutlined />
                    <span>{item.authorName || 'Ban Truyền Thông'}</span>
                  </Space>
                  <Space size="middle">
                    <Space size="small">
                      <CalendarOutlined />
                      <span>{dayjs(item.publishedAt).format('DD/MM/YYYY')}</span>
                    </Space>
                    <Space size="small">
                      <EyeOutlined />
                      <span>{item.viewsCount || 0}</span>
                    </Space>
                  </Space>
                </div>

                <Button
                  type="link"
                  style={{ padding: '8px 0 0 0', textAlign: 'left', fontWeight: 600, color: '#F26F21' }}
                >
                  Đọc tiếp →
                </Button>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </div>
  )
}

export default NewsPage
