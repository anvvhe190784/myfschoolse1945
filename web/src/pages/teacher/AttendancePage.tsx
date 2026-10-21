import React, { useState, useEffect, useCallback } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
  Card,
  Table,
  Button,
  Radio,
  Tag,
  Space,
  Typography,
  Progress,
  Row,
  Col,
  Statistic,
  Input,
  Select,
  Spin,
  message,
  Popconfirm,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  CheckSquareOutlined,
  SaveOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  UserOutlined,
  BookOutlined,
  ArrowLeftOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import apiClient from '@/api/client'
import {
  ScheduleItem,
  AttendanceRecord,
  AttendanceStatus,
  ScheduleAttendanceResponse,
} from '@/types'

const { Title, Text } = Typography

export const AttendancePage: React.FC = () => {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()

  const scheduleIdParam = searchParams.get('scheduleId')
  const classCodeParam = searchParams.get('classCode')

  const [schedules, setSchedules] = useState<ScheduleItem[]>([])
  const [selectedScheduleId, setSelectedScheduleId] = useState<number | null>(
    scheduleIdParam ? Number(scheduleIdParam) : null
  )
  const [currentSchedule, setCurrentSchedule] = useState<ScheduleItem | null>(null)
  const [students, setStudents] = useState<AttendanceRecord[]>([])
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [isSaved, setIsSaved] = useState(false)

  // Fetch teacher schedules for selector
  useEffect(() => {
    const fetchSchedules = async () => {
      try {
        const res = await apiClient.get<ScheduleItem[]>('/schedules')
        if (res.data && res.data.length > 0) {
          setSchedules(res.data)
          if (!selectedScheduleId) {
            setSelectedScheduleId(res.data[0].scheduleId)
            setCurrentSchedule(res.data[0])
          }
        }
      } catch {
        // Ignore
      }
    }
    void fetchSchedules()
  }, [])

  // When selected schedule changes, fetch detail & attendance
  const loadAttendance = useCallback(async (schedId: number) => {
    setLoading(true)
    setIsSaved(false)
    try {
      // 1. Fetch schedule detail
      const schedRes = await apiClient.get<ScheduleItem>(`/schedules/${schedId}`)
      setCurrentSchedule(schedRes.data)

      // 2. Fetch attendance
      try {
        const attRes = await apiClient.get<ScheduleAttendanceResponse>(`/schedules/${schedId}/attendance`)
        const rawList = attRes.data?.students || []
        const parsed: AttendanceRecord[] = rawList.map((s) => ({
          studentId: s.studentId,
          studentRollNumber: s.rollNumber || `SV${s.studentId}`,
          studentName: s.fullName || 'Sinh viên',
          status: (s.status as AttendanceStatus) || 'PRESENT',
          note: s.note || '',
        }))
        setStudents(parsed)
      } catch (attErr: any) {
        const errMsg =
          attErr.response?.data?.detail ||
          attErr.response?.data?.message ||
          'Không thể tải danh sách điểm danh của buổi học'
        message.error(errMsg)
        setStudents([])
      }
    } catch (schedErr: any) {
      const errMsg =
        schedErr.response?.data?.detail ||
        schedErr.response?.data?.message ||
        'Không tìm thấy thông tin ca dạy'
      message.error(errMsg)
      setStudents([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (selectedScheduleId) {
      void loadAttendance(selectedScheduleId)
    }
  }, [selectedScheduleId, loadAttendance])

  // Update status for single student
  const handleStatusChange = (studentId: number, status: AttendanceStatus) => {
    setStudents((prev) =>
      prev.map((s) => (s.studentId === studentId ? { ...s, status } : s))
    )
    setIsSaved(false)
  }

  // Update note for student
  const handleNoteChange = (studentId: number, note: string) => {
    setStudents((prev) =>
      prev.map((s) => (s.studentId === studentId ? { ...s, note } : s))
    )
  }

  // Mark all present
  const handleMarkAllPresent = () => {
    setStudents((prev) => prev.map((s) => ({ ...s, status: 'PRESENT' })))
    setIsSaved(false)
    message.info('Đã đánh dấu tất cả sinh viên CÓ MẶT!')
  }

  // Save attendance
  const handleSaveAttendance = async () => {
    if (!selectedScheduleId) return
    setSaving(true)
    try {
      await apiClient.put(`/schedules/${selectedScheduleId}/attendance`, {
        entries: students.map((s) => ({
          studentId: s.studentId,
          status: s.status,
          note: s.note || null,
        })),
      })
      setIsSaved(true)
      message.success('Lưu kết quả điểm danh thành công!')
    } catch (saveErr: any) {
      const errMsg =
        saveErr.response?.data?.detail ||
        saveErr.response?.data?.message ||
        'Lưu điểm danh thất bại. Vui lòng thử lại!'
      message.error(errMsg)
    } finally {
      setSaving(false)
    }
  }

  // Calculate statistics
  const totalCount = students.length
  const presentCount = students.filter((s) => s.status === 'PRESENT').length
  const absentCount = students.filter((s) => s.status === 'ABSENT').length
  const lateCount = students.filter((s) => s.status === 'LATE').length
  const attendanceRate = totalCount > 0 ? Math.round((presentCount / totalCount) * 100) : 0

  const columns: ColumnsType<AttendanceRecord> = [
    {
      title: 'STT',
      key: 'index',
      width: 60,
      align: 'center',
      render: (_, __, index) => index + 1,
    },
    {
      title: 'Mã SV (MSSV)',
      dataIndex: 'studentRollNumber',
      key: 'studentRollNumber',
      width: 140,
      render: (roll: string) => (
        <Text strong style={{ color: '#002766', fontFamily: 'monospace' }}>
          {roll}
        </Text>
      ),
    },
    {
      title: 'Họ và tên sinh viên',
      dataIndex: 'studentName',
      key: 'studentName',
      render: (name: string) => (
        <Space>
          <UserOutlined style={{ color: '#F26F21' }} />
          <Text strong>{name}</Text>
        </Space>
      ),
    },
    {
      title: 'Trạng thái điểm danh',
      key: 'status',
      width: 320,
      render: (_, record) => (
        <Radio.Group
          value={record.status}
          onChange={(e) => handleStatusChange(record.studentId, e.target.value)}
          size="middle"
        >
          <Radio.Button
            value="PRESENT"
            style={{
              color: record.status === 'PRESENT' ? '#fff' : '#52C41A',
              backgroundColor: record.status === 'PRESENT' ? '#52C41A' : undefined,
              borderColor: '#52C41A',
              fontWeight: 600,
            }}
          >
            <CheckCircleOutlined /> Có mặt
          </Radio.Button>
          <Radio.Button
            value="ABSENT"
            style={{
              color: record.status === 'ABSENT' ? '#fff' : '#F5222D',
              backgroundColor: record.status === 'ABSENT' ? '#F5222D' : undefined,
              borderColor: '#F5222D',
              fontWeight: 600,
            }}
          >
            <CloseCircleOutlined /> Vắng
          </Radio.Button>
          <Radio.Button
            value="LATE"
            style={{
              color: record.status === 'LATE' ? '#fff' : '#FAAD14',
              backgroundColor: record.status === 'LATE' ? '#FAAD14' : undefined,
              borderColor: '#FAAD14',
              fontWeight: 600,
            }}
          >
            <ClockCircleOutlined /> Muộn
          </Radio.Button>
        </Radio.Group>
      ),
    },
    {
      title: 'Ghi chú',
      key: 'note',
      width: 240,
      render: (_, record) => (
        <Input
          placeholder="Lý do, phép..."
          value={record.note || ''}
          onChange={(e) => handleNoteChange(record.studentId, e.target.value)}
          size="middle"
        />
      ),
    },
  ]

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      {/* Back button & Title */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 16,
          flexWrap: 'wrap',
          gap: 12,
        }}
      >
        <Space align="center">
          <Button
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/teacher/schedule')}
            style={{ borderColor: '#d9d9d9' }}
          >
            Về Lịch Giảng Dạy
          </Button>
          <Title level={3} style={{ margin: 0, color: '#002766' }}>
            <CheckSquareOutlined style={{ color: '#F26F21', marginRight: 8 }} />
            Điểm Danh Sinh Viên
          </Title>
        </Space>

        {/* Schedule Selector */}
        <Space>
          <Text strong>Chọn buổi học:</Text>
          <Select
            value={selectedScheduleId}
            onChange={(val) => {
              setSelectedScheduleId(val)
              const found = schedules.find((s) => s.scheduleId === val)
              if (found) setCurrentSchedule(found)
            }}
            style={{ minWidth: 280 }}
            options={schedules.map((s) => ({
              value: s.scheduleId,
              label: `${s.classCode} - ${s.courseCode} (Slot ${s.slotNumber}, ${dayjs(s.date).format(
                'DD/MM'
              )})`,
            }))}
            placeholder="Chọn lịch học..."
          />
        </Space>
      </div>

      {/* Class & Schedule Banner Card */}
      <Card
        style={{
          borderRadius: 12,
          marginBottom: 20,
          background: 'linear-gradient(135deg, #001529 0%, #002766 100%)',
          color: '#fff',
        }}
        styles={{ body: { padding: '20px 24px' } }}
      >
        <Row gutter={[24, 16]} align="middle">
          <Col xs={24} md={16}>
            <Space direction="vertical" size={4}>
              <Space wrap align="center">
                <Tag color="#F26F21" style={{ fontSize: 13, padding: '2px 10px', fontWeight: 700 }}>
                  {currentSchedule?.classCode || classCodeParam || 'LỚP HỌC PHẦN'}
                </Tag>
                <Title level={4} style={{ color: '#fff', margin: 0 }}>
                  {currentSchedule?.courseName || 'Môn học chuyên ngành FPT'}
                </Title>
                <Tag color="cyan">{currentSchedule?.courseCode || 'PRJ301'}</Tag>
              </Space>
              <Space wrap size="large" style={{ color: 'rgba(255,255,255,0.85)', fontSize: 13, marginTop: 4 }}>
                <span>
                  <BookOutlined style={{ marginRight: 6 }} />
                  Slot {currentSchedule?.slotNumber || 1} ({currentSchedule?.startTime?.slice(0, 5) || '07:30'} -{' '}
                  {currentSchedule?.endTime?.slice(0, 5) || '09:50'})
                </span>
                <span>Phòng: <strong>{currentSchedule?.room || 'P.302'}</strong></span>
                <span>
                  Ngày: <strong>{dayjs(currentSchedule?.date || dayjs()).format('DD/MM/YYYY')}</strong>
                </span>
              </Space>
            </Space>
          </Col>

          <Col xs={24} md={8} style={{ textAlign: 'right' }}>
            {isSaved && (
              <Tag color="success" style={{ padding: '6px 12px', fontSize: 13, fontWeight: 600 }}>
                ✓ ĐÃ LƯU ĐIỂM DANH
              </Tag>
            )}
          </Col>
        </Row>
      </Card>

      {/* KPI Stats Row */}
      <Row gutter={[16, 16]} style={{ marginBottom: 20 }}>
        <Col xs={12} sm={6}>
          <Card bordered={false} style={{ borderRadius: 10, textAlign: 'center', boxShadow: '0 2px 6px rgba(0,0,0,0.05)' }}>
            <Statistic
              title="Tổng số sinh viên"
              value={totalCount}
              valueStyle={{ color: '#002766', fontWeight: 700 }}
              suffix="SV"
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card bordered={false} style={{ borderRadius: 10, textAlign: 'center', boxShadow: '0 2px 6px rgba(0,0,0,0.05)' }}>
            <Statistic
              title="Có mặt (Present)"
              value={presentCount}
              valueStyle={{ color: '#52C41A', fontWeight: 700 }}
              prefix={<CheckCircleOutlined />}
              suffix={`(${attendanceRate}%)`}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card bordered={false} style={{ borderRadius: 10, textAlign: 'center', boxShadow: '0 2px 6px rgba(0,0,0,0.05)' }}>
            <Statistic
              title="Vắng (Absent)"
              value={absentCount}
              valueStyle={{ color: '#F5222D', fontWeight: 700 }}
              prefix={<CloseCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card bordered={false} style={{ borderRadius: 10, textAlign: 'center', boxShadow: '0 2px 6px rgba(0,0,0,0.05)' }}>
            <Statistic
              title="Đi muộn (Late)"
              value={lateCount}
              valueStyle={{ color: '#FAAD14', fontWeight: 700 }}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* Attendance Progress bar */}
      <div style={{ marginBottom: 20, padding: '0 8px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
          <Text strong>Tỷ lệ chuyên cần buổi học:</Text>
          <Text strong style={{ color: attendanceRate >= 80 ? '#52C41A' : '#FAAD14' }}>
            {attendanceRate}%
          </Text>
        </div>
        <Progress
          percent={attendanceRate}
          strokeColor={{
            '0%': '#FA8C16',
            '100%': '#52C41A',
          }}
          status={attendanceRate < 80 ? 'active' : 'normal'}
        />
      </div>

      {/* Main Student List Table Card */}
      <Card
        style={{ borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}
        title={
          <Space>
            <Title level={4} style={{ margin: 0 }}>
              Danh Sách Sinh Viên Lớp Học
            </Title>
            <Tag color="blue">{students.length} sinh viên</Tag>
          </Space>
        }
        extra={
          <Space>
            <Button
              type="default"
              icon={<CheckCircleOutlined />}
              onClick={handleMarkAllPresent}
              style={{ color: '#52C41A', borderColor: '#52C41A' }}
            >
              Đánh dấu tất cả có mặt
            </Button>

            <Popconfirm
              title="Lưu kết quả điểm danh"
              description="Bạn có chắc chắn muốn lưu thông tin điểm danh của buổi học này?"
              onConfirm={handleSaveAttendance}
              okText="Đồng ý lưu"
              cancelText="Hủy"
            >
              <Button
                type="primary"
                icon={<SaveOutlined />}
                loading={saving}
                style={{
                  background: 'linear-gradient(135deg, #F26F21 0%, #FA8C16 100%)',
                  borderColor: '#F26F21',
                  fontWeight: 600,
                }}
              >
                Lưu điểm danh
              </Button>
            </Popconfirm>
          </Space>
        }
      >
        {loading ? (
          <div style={{ textAlign: 'center', padding: '60px 0' }}>
            <Spin size="large" tip="Đang tải danh sách điểm danh..." />
          </div>
        ) : (
          <Table
            columns={columns}
            dataSource={students}
            rowKey="studentId"
            pagination={false}
            bordered
            size="middle"
          />
        )}
      </Card>
    </div>
  )
}

export default AttendancePage
