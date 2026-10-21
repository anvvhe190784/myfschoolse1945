import React, { useState, useEffect } from 'react'
import {
  Card,
  Row,
  Col,
  Input,
  Tag,
  Typography,
  Space,
  Empty,
  Spin,
  Avatar,
  Badge,
} from 'antd'
import {
  SearchOutlined,
  TeamOutlined,
  MailOutlined,
  UserOutlined,
  HeartOutlined,
} from '@ant-design/icons'
import apiClient from '@/api/client'
import { ClubItem } from '@/types'

const { Title, Text, Paragraph } = Typography
const { Search } = Input

export const ClubPage: React.FC = () => {
  const [clubs, setClubs] = useState<ClubItem[]>([])
  const [loading, setLoading] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')

  useEffect(() => {
    const fetchClubs = async () => {
      setLoading(true)
      try {
        const res = await apiClient.get<ClubItem[]>('/clubs')
        setClubs(res.data || [])
      } catch {
        // Fallback
      } finally {
        setLoading(false)
      }
    }
    void fetchClubs()
  }, [])

  const filteredClubs = clubs.filter(
    (c) =>
      c.clubName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (c.category && c.category.toLowerCase().includes(searchTerm.toLowerCase()))
  )

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
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
            <HeartOutlined style={{ color: '#F26F21', marginRight: 10 }} />
            Câu Lạc Bộ Sinh Viên F-School
          </Title>
          <Text type="secondary">Các cộng đồng tài năng, thể thao và học thuật sôi nổi của sinh viên</Text>
        </div>

        <Search
          placeholder="Tìm kiếm CLB theo tên, danh mục..."
          allowClear
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ width: 300 }}
          prefix={<SearchOutlined />}
        />
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '80px 0' }}>
          <Spin size="large" tip="Đang tải danh sách câu lạc bộ..." />
        </div>
      ) : filteredClubs.length === 0 ? (
        <Card style={{ borderRadius: 12, textAlign: 'center', padding: '40px 0' }}>
          <Empty description="Không tìm thấy câu lạc bộ nào" />
        </Card>
      ) : (
        <Row gutter={[24, 24]}>
          {filteredClubs.map((club) => (
            <Col xs={24} sm={12} lg={8} key={club.clubId}>
              <Card
                hoverable
                style={{
                  borderRadius: 12,
                  overflow: 'hidden',
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  boxShadow: '0 2px 10px rgba(0,0,0,0.06)',
                  border: '1px solid #f0f0f0',
                }}
                cover={
                  <div
                    style={{
                      height: 120,
                      position: 'relative',
                      background: club.bannerUrl
                        ? `url(${club.bannerUrl}) center/cover no-repeat`
                        : 'linear-gradient(135deg, #F26F21 0%, #002766 100%)',
                    }}
                  >
                    <div style={{ position: 'absolute', top: 12, right: 12 }}>
                      <Tag color={club.isActive ? 'success' : 'default'}>
                        {club.isActive ? 'ĐANG HOẠT ĐỘNG' : 'TẠM DỪNG'}
                      </Tag>
                    </div>
                  </div>
                }
                styles={{
                  body: {
                    flex: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    padding: '0 20px 20px 20px',
                  },
                }}
              >
                {/* Avatar floating over banner */}
                <div style={{ marginTop: -32, marginBottom: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
                  <Avatar
                    size={64}
                    src={club.avatarUrl}
                    icon={<TeamOutlined />}
                    style={{
                      border: '3px solid #fff',
                      boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
                      backgroundColor: '#F26F21',
                    }}
                  />
                  <Badge
                    count={`${club.memberCount || 0} thành viên`}
                    style={{ backgroundColor: '#1890FF' }}
                  />
                </div>

                <Title level={5} style={{ margin: '0 0 6px 0', color: '#002766' }}>
                  {club.clubName}
                </Title>

                <div style={{ marginBottom: 10 }}>
                  <Tag color="cyan">{club.category || 'HỌC THUẬT'}</Tag>
                </div>

                <Paragraph
                  ellipsis={{ rows: 3 }}
                  type="secondary"
                  style={{ fontSize: 13, flex: 1, marginBottom: 16 }}
                >
                  {club.description}
                </Paragraph>

                <div
                  style={{
                    borderTop: '1px solid #f5f5f5',
                    paddingTop: 12,
                    fontSize: 12,
                    color: '#8c8c8c',
                  }}
                >
                  <div style={{ marginBottom: 4 }}>
                    <UserOutlined style={{ marginRight: 6 }} />
                    Chủ nhiệm: <strong>{club.leaderName || 'Đang cập nhật'}</strong>
                  </div>
                  {club.contactEmail && (
                    <div>
                      <MailOutlined style={{ marginRight: 6 }} />
                      Email: <span>{club.contactEmail}</span>
                    </div>
                  )}
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      )}
    </div>
  )
}

export default ClubPage
