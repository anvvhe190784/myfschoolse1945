import React, { useState, useEffect, useCallback } from 'react'
import {
  Card,
  Table,
  Button,
  Tag,
  Space,
  Typography,
  DatePicker,
  Select,
  Modal,
  Form,
  Input,
  InputNumber,
  Row,
  Col,
  TimePicker,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  CalendarOutlined,
  PlusOutlined,
  ClockCircleOutlined,
  EnvironmentOutlined,
  UserOutlined,
} from '@ant-design/icons'
import dayjs, { Dayjs } from 'dayjs'
import apiClient from '@/api/client'
import { ScheduleItem, ClassItem, User } from '@/types'

const { Title, Text } = Typography

const SLOT_TIMES: Record<number, { start: string; end: string }> = {
  1: { start: '07:30:00', end: '09:50:00' },
  2: { start: '10:00:00', end: '12:20:00' },
  3: { start: '12:50:00', end: '15:10:00' },
  4: { start: '15:20:00', end: '17:40:00' },
  5: { start: '18:00:00', end: '20:20:00' },
  6: { start: '20:30:00', end: '22:50:00' },
}

export const ScheduleManagementPage: React.FC = () => {
  const [schedules, setSchedules] = useState<ScheduleItem[]>([])
  const [loading, setLoading] = useState(false)
  const [filterDate, setFilterDate] = useState<Dayjs | null>(null)

  // Classes & Lecturers for Form
  const [classes, setClasses] = useState<ClassItem[]>([])
  const [lecturers, setLecturers] = useState<User[]>([])

  // Create Modal
  const [createModalOpen, setCreateModalOpen] = useState(false)
  const [createLoading, setCreateLoading] = useState(false)
  const [form] = Form.useForm()

  const fetchSchedules = useCallback(async () => {
    setLoading(true)
    try {
      const params: Record<string, string> = {}
      if (filterDate) {
        params.date = filterDate.format('YYYY-MM-DD')
      }
      const res = await apiClient.get<ScheduleItem[]>('/schedules', { params })
      setSchedules(res.data || [])
    } catch {
      message.error('Không thể tải dữ liệu thời khóa biểu')
    } finally {
      setLoading(false)
    }
  }, [filterDate])

  useEffect(() => {
    void fetchSchedules()
  }, [fetchSchedules])

  const handleOpenCreateModal = async () => {
    try {
      const [cRes, uRes] = await Promise.all([
        apiClient.get<ClassItem[]>('/admin/academic/classes', { params: { size: 50 } }),
        apiClient.get<User[]>('/admin/users', { params: { size: 50 } }),
      ])
      setClasses(cRes.data || [])
      setLecturers((uRes.data || []).filter((u) => u.role === 'LECTURER'))
      setCreateModalOpen(true)
    } catch {
      message.error('Không thể tải danh sách lớp học và giảng viên')
    }
  }

  const handleSlotChange = (slot: number) => {
    if (SLOT_TIMES[slot]) {
      const { start, end } = SLOT_TIMES[slot]
      form.setFieldsValue({
        startTime: dayjs(`2024-01-01 ${start}`),
        endTime: dayjs(`2024-01-01 ${end}`),
      })
    }
  }

  const handleCreateSchedule = async (values: {
    classId: number
    slotNumber: number
    sessionNo?: number
    date: Dayjs
    startTime: Dayjs
    endTime: Dayjs
    room: string
    lecturerId?: number
    note?: string
  }) => {
    setCreateLoading(true)
    try {
      const payload = {
        classId: values.classId,
        slotNumber: values.slotNumber,
        sessionNo: values.sessionNo || 1,
        date: values.date.format('YYYY-MM-DD'),
        startTime: values.startTime.format('HH:mm:ss'),
        endTime: values.endTime.format('HH:mm:ss'),
        room: values.room,
        lecturerId: values.lecturerId,
        note: values.note,
      }

      await apiClient.post('/admin/academic/schedules', payload)
      message.success('Xếp lịch học mới thành công!')
      setCreateModalOpen(false)
      form.resetFields()
      void fetchSchedules()
    } catch (err: unknown) {
      const msg =
        (err as { rfcMessage?: string })?.rfcMessage || 'Xếp lịch học thất bại. Vui lòng kiểm tra lại!'
      message.error(msg)
    } finally {
      setCreateLoading(false)
    }
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
          <div style={{ fontSize: 11, color: '#8c8c8c' }}>{dayjs(date).format('dddd')}</div>
        </div>
      ),
    },
    {
      title: 'Slot & Giờ',
      key: 'slot',
      width: 150,
      render: (_, record) => (
        <Space orientation="vertical" size={2}>
          <Tag color="cyan">Slot {record.slotNumber} (Buổi {record.sessionNo || 1})</Tag>
          <div style={{ fontSize: 12, color: '#595959' }}>
            <ClockCircleOutlined style={{ marginRight: 4 }} />
            {record.startTime?.slice(0, 5)} - {record.endTime?.slice(0, 5)}
          </div>
        </Space>
      ),
    },
    {
      title: 'Lớp & Môn học',
      key: 'class',
      render: (_, record) => (
        <div>
          <Text strong style={{ color: '#002766', fontSize: 14 }}>{record.classCode}</Text>
          <div style={{ fontSize: 12, color: '#595959' }}>{record.courseName} ({record.courseCode})</div>
        </div>
      ),
    },
    {
      title: 'Phòng học',
      dataIndex: 'room',
      key: 'room',
      width: 110,
      render: (room: string) => (
        <Tag color="purple" icon={<EnvironmentOutlined />}>
          {room}
        </Tag>
      ),
    },
    {
      title: 'Giảng viên',
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
      title: 'Ghi chú',
      dataIndex: 'note',
      key: 'note',
      render: (note: string) => <Text type="secondary">{note || '—'}</Text>,
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
            <CalendarOutlined style={{ color: '#F26F21', marginRight: 10 }} />
            Thời Khóa Biểu Toàn Trường
          </Title>
          <Text type="secondary">Quản lý lịch học, phòng học và điều phối ca giảng dạy</Text>
        </div>

        <Button
          type="primary"
          icon={<PlusOutlined />}
          size="large"
          onClick={handleOpenCreateModal}
          style={{ background: '#F26F21', borderColor: '#F26F21', fontWeight: 600 }}
        >
          Xếp lịch học mới
        </Button>
      </div>

      {/* Filter */}
      <Card
        style={{ borderRadius: 12, marginBottom: 20, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}
        styles={{ body: { padding: '16px 20px' } }}
      >
        <Space wrap size="middle">
          <Text strong>Lọc theo ngày:</Text>
          <DatePicker
            value={filterDate}
            onChange={(val) => setFilterDate(val)}
            placeholder="Tất cả các ngày"
            format="DD/MM/YYYY"
          />
          {filterDate && (
            <Button onClick={() => setFilterDate(null)}>
              Xóa bộ lọc ngày
            </Button>
          )}
        </Space>
      </Card>

      {/* Table */}
      <Card style={{ borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
        <Table
          columns={columns}
          dataSource={schedules}
          rowKey="scheduleId"
          loading={loading}
          pagination={{ pageSize: 15 }}
          locale={{ emptyText: 'Không tìm thấy lịch học nào' }}
        />
      </Card>

      {/* Create Schedule Modal */}
      <Modal
        title={
          <Space>
            <CalendarOutlined style={{ color: '#F26F21' }} />
            <span>Xếp ca học mới cho lớp học phần</span>
          </Space>
        }
        open={createModalOpen}
        onCancel={() => {
          setCreateModalOpen(false)
          form.resetFields()
        }}
        footer={null}
        destroyOnClose
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleCreateSchedule}>
          <Form.Item
            name="classId"
            label="Lớp học phần"
            rules={[{ required: true, message: 'Vui lòng chọn lớp học phần' }]}
          >
            <Select
              placeholder="Chọn lớp học phần"
              options={classes.map((c) => ({
                value: c.classId,
                label: `${c.classCode} - ${c.courseName} (${c.semester})`,
              }))}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="slotNumber"
                label="Slot học (1 - 6)"
                initialValue={1}
                rules={[{ required: true, message: 'Chọn slot học' }]}
              >
                <Select
                  onChange={handleSlotChange}
                  options={[
                    { value: 1, label: 'Slot 1 (07:30 - 09:50)' },
                    { value: 2, label: 'Slot 2 (10:00 - 12:20)' },
                    { value: 3, label: 'Slot 3 (12:50 - 15:10)' },
                    { value: 4, label: 'Slot 4 (15:20 - 17:40)' },
                    { value: 5, label: 'Slot 5 (18:00 - 20:20)' },
                    { value: 6, label: 'Slot 6 (20:30 - 22:50)' },
                  ]}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="sessionNo" label="Buổi số" initialValue={1}>
                <InputNumber min={1} max={60} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="date"
                label="Ngày diễn ra"
                initialValue={dayjs()}
                rules={[{ required: true, message: 'Chọn ngày học' }]}
              >
                <DatePicker style={{ width: '100%' }} format="DD/MM/YYYY" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="room"
                label="Phòng học"
                rules={[{ required: true, message: 'Nhập phòng học' }]}
              >
                <Input placeholder="Ví dụ: P.302, Lab 2" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="startTime"
                label="Giờ bắt đầu"
                initialValue={dayjs('2024-01-01 07:30:00')}
                rules={[{ required: true }]}
              >
                <TimePicker style={{ width: '100%' }} format="HH:mm" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="endTime"
                label="Giờ kết thúc"
                initialValue={dayjs('2024-01-01 09:50:00')}
                rules={[{ required: true }]}
              >
                <TimePicker style={{ width: '100%' }} format="HH:mm" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="lecturerId" label="Giảng viên dạy (tùy chọn)">
            <Select
              allowClear
              placeholder="Chọn giảng viên đứng lớp"
              options={lecturers.map((l) => ({
                value: l.userId,
                label: `${l.fullName} (${l.email})`,
              }))}
            />
          </Form.Item>

          <Form.Item name="note" label="Ghi chú buổi học">
            <Input placeholder="Ví dụ: Thi PE, Buổi phụ đạo, Lab thực hành..." />
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
                Lưu lịch học
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ScheduleManagementPage
