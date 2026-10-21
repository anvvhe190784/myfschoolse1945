import React, { useState, useEffect, useCallback } from 'react'
import {
  Card,
  List,
  Button,
  Tag,
  Space,
  Typography,
  Radio,
  Empty,
  Badge,
  Spin,
  message,
} from 'antd'
import {
  BellOutlined,
  CheckOutlined,
  CalendarOutlined,
  BookOutlined,
  FileTextOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import apiClient from '@/api/client'
import { NotificationItem } from '@/types'

const { Title, Text, Paragraph } = Typography

export const NotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([])
  const [loading, setLoading] = useState(false)
  const [unreadOnly, setUnreadOnly] = useState(false)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [totalElements, setTotalElements] = useState(0)
  const [markingAll, setMarkingAll] = useState(false)

  const fetchNotifications = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiClient.get<{
        content: NotificationItem[]
        totalElements: number
      }>('/notifications', {
        params: {
          page,
          size: pageSize,
          unreadOnly,
        },
      })
      setNotifications(res.data.content || [])
      setTotalElements(res.data.totalElements || 0)
    } catch {
      message.error('Không thể tải danh sách thông báo')
    } finally {
      setLoading(false)
    }
  }, [page, pageSize, unreadOnly])

  useEffect(() => {
    void fetchNotifications()
  }, [fetchNotifications])

  const handleMarkAsRead = async (id: number) => {
    try {
      await apiClient.patch(`/notifications/${id}/read`)
      setNotifications((prev) =>
        prev.map((item) => (item.notificationId === id ? { ...item, isRead: true } : item))
      )
    } catch {
      // Ignore
    }
  }

  const handleMarkAllAsRead = async () => {
    try {
      setMarkingAll(true)
      await apiClient.patch('/notifications/read-all')
      message.success('Đã đánh dấu tất cả thông báo là đã đọc!')
      setNotifications((prev) => prev.map((item) => ({ ...item, isRead: true })))
    } catch {
      message.error('Thao tác thất bại')
    } finally {
      setMarkingAll(false)
    }
  }

  const getTypeTag = (type: string) => {
    switch (type) {
      case 'SCHEDULE':
        return (
          <Tag color="orange" icon={<CalendarOutlined />}>
            LỊCH HỌC
          </Tag>
        )
      case 'ACADEMIC':
        return (
          <Tag color="blue" icon={<BookOutlined />}>
            HỌC VỤ
          </Tag>
        )
      case 'REQUEST':
        return (
          <Tag color="purple" icon={<FileTextOutlined />}>
            ĐƠN TỪ
          </Tag>
        )
      default:
        return (
          <Tag color="cyan" icon={<InfoCircleOutlined />}>
            THÔNG BÁO CHUNG
          </Tag>
        )
    }
  }

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto' }}>
      <Card
        title={
          <Space align="center">
            <BellOutlined style={{ color: '#F26F21', fontSize: 20 }} />
            <Title level={4} style={{ margin: 0 }}>
              Trung tâm Thông báo
            </Title>
          </Space>
        }
        extra={
          <Space>
            <Radio.Group
              value={unreadOnly}
              onChange={(e) => {
                setUnreadOnly(e.target.value)
                setPage(0)
              }}
              buttonStyle="solid"
            >
              <Radio.Button value={false}>Tất cả</Radio.Button>
              <Radio.Button value={true}>Chưa đọc</Radio.Button>
            </Radio.Group>
            <Button
              icon={<CheckOutlined />}
              onClick={handleMarkAllAsRead}
              loading={markingAll}
              disabled={notifications.length === 0}
            >
              Đánh dấu tất cả đã đọc
            </Button>
          </Space>
        }
        style={{ borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}
      >
        {loading && notifications.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '60px 0' }}>
            <Spin size="large" tip="Đang tải danh sách thông báo..." />
          </div>
        ) : notifications.length === 0 ? (
          <Empty description="Không có thông báo nào" style={{ padding: '40px 0' }} />
        ) : (
          <List
            itemLayout="horizontal"
            dataSource={notifications}
            pagination={{
              current: page + 1,
              pageSize,
              total: totalElements,
              onChange: (p, ps) => {
                setPage(p - 1)
                setPageSize(ps)
              },
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50'],
            }}
            renderItem={(item) => (
              <List.Item
                style={{
                  background: item.isRead ? '#ffffff' : '#F6FFED',
                  padding: '16px 20px',
                  borderRadius: 8,
                  marginBottom: 12,
                  border: item.isRead ? '1px solid #f0f0f0' : '1px solid #B7EB8F',
                  cursor: item.isRead ? 'default' : 'pointer',
                  transition: 'all 0.2s',
                }}
                onClick={() => {
                  if (!item.isRead) void handleMarkAsRead(item.notificationId)
                }}
                actions={[
                  !item.isRead && (
                    <Button
                      type="link"
                      size="small"
                      onClick={(e) => {
                        e.stopPropagation()
                        void handleMarkAsRead(item.notificationId)
                      }}
                    >
                      Đánh dấu đã đọc
                    </Button>
                  ),
                ].filter(Boolean)}
              >
                <List.Item.Meta
                  avatar={
                    <Badge dot={!item.isRead} offset={[-2, 5]} color="#52C41A">
                      <div
                        style={{
                          width: 40,
                          height: 40,
                          borderRadius: 8,
                          background: item.isRead ? '#f5f5f5' : '#E6F7FF',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          color: item.isRead ? '#8c8c8c' : '#1890FF',
                          fontSize: 18,
                        }}
                      >
                        <BellOutlined />
                      </div>
                    </Badge>
                  }
                  title={
                    <Space wrap align="center">
                      {getTypeTag(item.type)}
                      <Text strong style={{ fontSize: 15, color: '#262626' }}>
                        {item.title}
                      </Text>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        • {dayjs(item.createdAt).format('HH:mm DD/MM/YYYY')}
                      </Text>
                    </Space>
                  }
                  description={
                    <Paragraph
                      style={{
                        margin: '6px 0 0 0',
                        color: '#595959',
                        fontSize: 13,
                        lineHeight: 1.5,
                      }}
                    >
                      {item.content}
                    </Paragraph>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Card>
    </div>
  )
}

export default NotificationsPage
