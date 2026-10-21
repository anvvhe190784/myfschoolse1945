import React, { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Card,
  Table,
  Button,
  Tag,
  Space,
  Typography,
  Select,
  DatePicker,
  Radio,
  Row,
  Col,
  Statistic,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  CalendarOutlined,
  CheckSquareOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  LeftOutlined,
  RightOutlined,
} from '@ant-design/icons'
import dayjs, { Dayjs } from 'dayjs'
import apiClient from '@/api/client'
import { ScheduleItem } from '@/types'

const { Title, Text } = Typography

export const TeacherSchedulePage: React.FC = () => {
  const navigate = useNavigate()
  const [schedules, setSchedules] = useState<ScheduleItem[]>([])
  const [loading, setLoading] = useState(false)
  const [semesters, setSemesters] = useState<string[]>([])
  const [selectedSemester, setSelectedSemester] = useState<string>('')
  const [currentDate, setCurrentDate] = useState<Dayjs>(dayjs())
  const [viewMode, setViewMode] = useState<'WEEK' | 'DAY'>('WEEK')

  // Fetch semesters
  useEffect(() => {
    const fetchSemesters = async () => {
      try {
        const res = await apiClient.get<string[]>('/schedules/semesters')
        if (res.data && res.data.length > 0) {
          setSemesters(res.data)
          setSelectedSemester(res.data[0])
        }
      } catch {
        // Fallback default semesters
        setSemesters(['FA24', 'SP25', 'SU25'])
        setSelectedSemester('FA24')
      }
    }
    void fetchSemesters()
  }, [])

  // Fetch schedules for current date / week
  const fetchSchedules = useCallback(async () => {
    setLoading(true)
    try {
      let start: string
      let end: string

      if (viewMode === 'WEEK') {
        start = currentDate.startOf('week').add(1, 'day').format('YYYY-MM-DD') // Monday
        end = currentDate.startOf('week').add(7, 'day').format('YYYY-MM-DD') // Sunday
      } else {
        start = currentDate.format('YYYY-MM-DD')
        end = currentDate.format('YYYY-MM-DD')
      }

      const res = await apiClient.get<ScheduleItem[]>('/schedules', {
        params: {
          startDate: start,
          endDate: end,
        },
      })
      setSchedules(res.data || [])
    } catch {
      // If error or empty
      setSchedules([])
    } finally {
      setLoading(false)
    }
  }, [currentDate, viewMode])

  useEffect(() => {
    void fetchSchedules()
  }, [fetchSchedules])

  const handlePrev = () => {
    setCurrentDate((prev) => (viewMode === 'WEEK' ? prev.subtract(1, 'week') : prev.subtract(1, 'day')))
  }

  const handleNext = () => {
    setCurrentDate((prev) => (viewMode === 'WEEK' ? prev.add(1, 'week') : prev.add(1, 'day')))
  }

  const handleToday = () => {
    setCurrentDate(dayjs())
  }

  const columns: ColumnsType<ScheduleItem> = [
    {
      title: 'Ngày học',
      dataIndex: 'date',
      key: 'date',
      width: 130,
      render: (date: string) => (
        <div>
          <Text strong>{dayjs(date).format('DD/MM/YYYY')}</Text>
          <div style={{ fontSize: 11, color: '#8c8c8c' }}>
            {dayjs(date).format('dddd')}
          </div>
        </div>
      ),
    },
    {
      title: 'Slot & Giờ học',
      key: 'slot',
      width: 160,
      render: (_, record) => (
        <Space orientation="vertical" size={2}>
          <Tag color="blue" style={{ fontWeight: 600 }}>
            Slot {record.slotNumber} (Buổi {record.sessionNo || 1})
          </Tag>
          <div style={{ fontSize: 12, color: '#595959' }}>
            <ClockCircleOutlined style={{ marginRight: 4 }} />
            {record.startTime?.slice(0, 5)} - {record.endTime?.slice(0, 5)}
          </div>
        </Space>
      ),
    },
    {
      title: 'Lớp học phần',
      key: 'class',
      render: (_, record) => (
        <div>
          <Text strong style={{ color: '#002766', fontSize: 14 }}>
            {record.classCode}
          </Text>
          <div style={{ fontSize: 12, color: '#595959' }}>
            {record.courseCode} - {record.courseName}
          </div>
        </div>
      ),
    },
    {
      title: 'Phòng học',
      dataIndex: 'room',
      key: 'room',
      width: 110,
      render: (room: string) => (
        <Tag color="cyan" icon={<EnvironmentOutlined />}>
          {room}
        </Tag>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: string) => {
        const isCompleted = status === 'COMPLETED'
        return (
          <Tag color={isCompleted ? 'success' : 'processing'}>
            {isCompleted ? 'ĐÃ HOÀN THÀNH' : 'SẮP DIỄN RA'}
          </Tag>
        )
      },
    },
    {
      title: 'Ghi chú',
      dataIndex: 'note',
      key: 'note',
      render: (note: string) => <Text type="secondary">{note || '—'}</Text>,
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Button
          type="primary"
          icon={<CheckSquareOutlined />}
          size="middle"
          style={{ background: '#F26F21', borderColor: '#F26F21', fontWeight: 600 }}
          onClick={() =>
            navigate(
              `/teacher/attendance?scheduleId=${record.scheduleId}&classId=${record.classId}&classCode=${encodeURIComponent(
                record.classCode
              )}`
            )
          }
        >
          Điểm danh
        </Button>
      ),
    },
  ]

  // Calculate statistics for current period
  const totalSlots = schedules.length
  const todaySchedules = schedules.filter((s) => s.date === dayjs().format('YYYY-MM-DD'))

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      {/* Top Banner Stats */}
      <Row gutter={[16, 16]} style={{ marginBottom: 20 }}>
        <Col xs={24} sm={12} md={8}>
          <Card
            bordered={false}
            style={{
              borderRadius: 12,
              background: 'linear-gradient(135deg, #002766 0%, #003a8c 100%)',
              color: '#fff',
            }}
          >
            <Statistic
              title={<span style={{ color: 'rgba(255,255,255,0.85)' }}>Tiết dạy trong đợt này</span>}
              value={totalSlots}
              valueStyle={{ color: '#fff', fontWeight: 700 }}
              prefix={<CalendarOutlined />}
              suffix="buổi"
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={8}>
          <Card
            bordered={false}
            style={{
              borderRadius: 12,
              background: 'linear-gradient(135deg, #F26F21 0%, #FA8C16 100%)',
              color: '#fff',
            }}
          >
            <Statistic
              title={<span style={{ color: 'rgba(255,255,255,0.85)' }}>Lịch giảng hôm nay</span>}
              value={todaySchedules.length}
              valueStyle={{ color: '#fff', fontWeight: 700 }}
              prefix={<ClockCircleOutlined />}
              suffix="ca học"
            />
          </Card>
        </Col>
        <Col xs={24} sm={24} md={8}>
          <Card
            bordered={false}
            style={{
              borderRadius: 12,
              background: '#ffffff',
              boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
            }}
          >
            <Text type="secondary" style={{ fontSize: 13, display: 'block', marginBottom: 4 }}>
              Học kỳ giảng dạy hiện tại
            </Text>
            <Select
              value={selectedSemester}
              onChange={setSelectedSemester}
              style={{ width: '100%' }}
              options={semesters.map((s) => ({ value: s, label: `Học kỳ ${s}` }))}
            />
          </Card>
        </Col>
      </Row>

      {/* Main Schedule Table Card */}
      <Card
        style={{ borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}
        title={
          <Space align="center" wrap>
            <CalendarOutlined style={{ color: '#F26F21', fontSize: 20 }} />
            <Title level={4} style={{ margin: 0 }}>
              Thời Khóa Biểu Giảng Dạy
            </Title>
            <Tag color="orange">{viewMode === 'WEEK' ? 'Theo tuần' : 'Theo ngày'}</Tag>
          </Space>
        }
        extra={
          <Space wrap>
            <Radio.Group
              value={viewMode}
              onChange={(e) => setViewMode(e.target.value)}
              buttonStyle="solid"
            >
              <Radio.Button value="WEEK">Theo tuần</Radio.Button>
              <Radio.Button value="DAY">Theo ngày</Radio.Button>
            </Radio.Group>

            <Space>
              <Button icon={<LeftOutlined />} onClick={handlePrev} />
              <Button onClick={handleToday}>Hôm nay</Button>
              <Button icon={<RightOutlined />} onClick={handleNext} />
            </Space>

            <DatePicker
              value={currentDate}
              onChange={(val) => val && setCurrentDate(val)}
              allowClear={false}
              format="DD/MM/YYYY"
            />
          </Space>
        }
      >
        <div style={{ marginBottom: 16 }}>
          <Text strong style={{ fontSize: 15, color: '#002766' }}>
            {viewMode === 'WEEK'
              ? `Tuần từ ${currentDate.startOf('week').add(1, 'day').format('DD/MM/YYYY')} đến ${currentDate
                  .startOf('week')
                  .add(7, 'day')
                  .format('DD/MM/YYYY')}`
              : `Ngày ${currentDate.format('DD/MM/YYYY')} (${currentDate.format('dddd')})`}
          </Text>
        </div>

        <Table
          columns={columns}
          dataSource={schedules}
          rowKey="scheduleId"
          loading={loading}
          pagination={false}
          locale={{ emptyText: 'Không có lịch giảng dạy trong khoảng thời gian này' }}
        />
      </Card>
    </div>
  )
}

export default TeacherSchedulePage
