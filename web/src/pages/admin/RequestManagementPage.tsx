import React, { useState, useEffect, useCallback } from 'react'
import {
  Card,
  Table,
  Button,
  Tag,
  Space,
  Typography,
  Select,
  Modal,
  Form,
  Input,
  Radio,
  message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
  FileDoneOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  PaperClipOutlined,
  SearchOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import apiClient from '@/api/client'
import { RequestItem } from '@/types'

const { Title, Text, Paragraph } = Typography
const { Search } = Input

export const RequestManagementPage: React.FC = () => {
  const [requests, setRequests] = useState<RequestItem[]>([])
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [typeFilter, setTypeFilter] = useState<string>('ALL')

  // Process Modal
  const [processModalOpen, setProcessModalOpen] = useState(false)
  const [selectedRequest, setSelectedRequest] = useState<RequestItem | null>(null)
  const [processLoading, setProcessLoading] = useState(false)
  const [form] = Form.useForm()

  const fetchRequests = useCallback(async () => {
    setLoading(true)
    try {
      const res = await apiClient.get<RequestItem[]>('/admin/academic/requests', {
        params: { page, size: pageSize },
      })
      setRequests(res.data || [])
    } catch {
      message.error('Không thể tải danh sách đơn từ')
    } finally {
      setLoading(false)
    }
  }, [page, pageSize])

  useEffect(() => {
    void fetchRequests()
  }, [fetchRequests])

  const handleOpenProcessModal = (record: RequestItem) => {
    setSelectedRequest(record)
    form.setFieldsValue({
      status: record.status === 'PENDING' ? 'APPROVED' : record.status,
      responseNote: record.responseNote || '',
    })
    setProcessModalOpen(true)
  }

  const handleProcessSubmit = async (values: { status: string; responseNote?: string }) => {
    if (!selectedRequest) return
    setProcessLoading(true)
    try {
      await apiClient.patch(
        `/admin/academic/requests/${selectedRequest.requestId}/process`,
        {
          status: values.status,
          responseNote: values.responseNote || '',
        }
      )
      message.success('Xử lý đơn từ thành công!')
      setRequests((prev) =>
        prev.map((r) =>
          r.requestId === selectedRequest.requestId
            ? { ...r, status: values.status, responseNote: values.responseNote }
            : r
        )
      )
      setProcessModalOpen(false)
    } catch (err: unknown) {
      const msg =
        (err as { rfcMessage?: string })?.rfcMessage || 'Xử lý đơn từ thất bại'
      message.error(msg)
    } finally {
      setProcessLoading(false)
    }
  }

  const filteredRequests = requests.filter((r) => {
    const matchSearch =
      r.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.studentName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.studentRollNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      r.content.toLowerCase().includes(searchTerm.toLowerCase())

    const matchStatus = statusFilter === 'ALL' || r.status === statusFilter
    const matchType = typeFilter === 'ALL' || r.requestType === typeFilter
    return matchSearch && matchStatus && matchType
  })

  const getRequestTypeTag = (type: string) => {
    switch (type) {
      case 'ATTENDANCE_EXEMPTION':
        return <Tag color="orange">Miễn điểm danh</Tag>
      case 'LEAVE_OF_ABSENCE':
        return <Tag color="blue">Nghỉ phép</Tag>
      case 'COURSE_WITHDRAWAL':
        return <Tag color="purple">Rút môn học</Tag>
      default:
        return <Tag color="cyan">{type}</Tag>
    }
  }

  const columns: ColumnsType<RequestItem> = [
    {
      title: 'Mã đơn',
      dataIndex: 'requestId',
      key: 'requestId',
      width: 90,
      render: (id: number) => <Text code>#{id}</Text>,
    },
    {
      title: 'Sinh viên nộp đơn',
      key: 'student',
      width: 200,
      render: (_, record) => (
        <div>
          <Text strong style={{ color: '#002766' }}>{record.studentName}</Text>
          <div style={{ fontSize: 11, color: '#8c8c8c', fontFamily: 'monospace' }}>
            {record.studentRollNumber}
          </div>
        </div>
      ),
    },
    {
      title: 'Loại đơn',
      dataIndex: 'requestType',
      key: 'requestType',
      width: 150,
      render: (type: string) => getRequestTypeTag(type),
    },
    {
      title: 'Tiêu đề & Nội dung',
      key: 'titleContent',
      render: (_, record) => (
        <div>
          <Text strong>{record.title}</Text>
          <Paragraph ellipsis={{ rows: 2 }} type="secondary" style={{ margin: '4px 0 0 0', fontSize: 12 }}>
            {record.content}
          </Paragraph>
          {record.attachmentUrl && (
            <div style={{ marginTop: 4 }}>
              <a href={record.attachmentUrl} target="_blank" rel="noopener noreferrer">
                <PaperClipOutlined /> Tệp đính kèm
              </a>
            </div>
          )}
        </div>
      ),
    },
    {
      title: 'Thời gian gửi',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 140,
      render: (date: string) => (
        <span style={{ fontSize: 12, color: '#595959' }}>
          {dayjs(date).format('HH:mm DD/MM/YY')}
        </span>
      ),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      width: 130,
      render: (status: string) => {
        if (status === 'APPROVED') return <Tag color="success">ĐÃ DUYỆT</Tag>
        if (status === 'REJECTED') return <Tag color="error">TỪ CHỐI</Tag>
        return <Tag color="warning">CHỜ DUYỆT</Tag>
      },
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 110,
      align: 'center',
      render: (_, record) => (
        <Button
          type="primary"
          size="small"
          onClick={() => handleOpenProcessModal(record)}
          style={{
            background: record.status === 'PENDING' ? '#F26F21' : '#1890FF',
            borderColor: record.status === 'PENDING' ? '#F26F21' : '#1890FF',
          }}
        >
          {record.status === 'PENDING' ? 'Xử lý' : 'Xem lại'}
        </Button>
      ),
    },
  ]

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto' }}>
      {/* Header */}
      <div style={{ marginBottom: 20 }}>
        <Title level={3} style={{ margin: 0, color: '#002766' }}>
          <FileDoneOutlined style={{ color: '#F26F21', marginRight: 10 }} />
          Duyệt Đơn Từ Sinh Viên
        </Title>
        <Text type="secondary">Xử lý các đề xuất xin nghỉ học, miễn điểm danh và khiếu nại học vụ</Text>
      </div>

      {/* Filter toolbar */}
      <Card
        style={{ borderRadius: 12, marginBottom: 20, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}
        styles={{ body: { padding: '16px 20px' } }}
      >
        <Space wrap size="middle">
          <Search
            placeholder="Tìm theo tiêu đề, MSSV, tên SV..."
            allowClear
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: 300 }}
            prefix={<SearchOutlined />}
          />

          <Select
            value={statusFilter}
            onChange={setStatusFilter}
            style={{ width: 160 }}
            options={[
              { value: 'ALL', label: 'Tất cả trạng thái' },
              { value: 'PENDING', label: 'Chờ duyệt (PENDING)' },
              { value: 'APPROVED', label: 'Đã duyệt (APPROVED)' },
              { value: 'REJECTED', label: 'Từ chối (REJECTED)' },
            ]}
          />

          <Select
            value={typeFilter}
            onChange={setTypeFilter}
            style={{ width: 190 }}
            options={[
              { value: 'ALL', label: 'Tất cả loại đơn' },
              { value: 'ATTENDANCE_EXEMPTION', label: 'Miễn điểm danh' },
              { value: 'LEAVE_OF_ABSENCE', label: 'Nghỉ phép' },
              { value: 'COURSE_WITHDRAWAL', label: 'Rút môn học' },
            ]}
          />
        </Space>
      </Card>

      {/* Table */}
      <Card style={{ borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
        <Table
          columns={columns}
          dataSource={filteredRequests}
          rowKey="requestId"
          loading={loading}
          pagination={{
            current: page + 1,
            pageSize,
            total: filteredRequests.length,
            onChange: (p, ps) => {
              setPage(p - 1)
              setPageSize(ps)
            },
            showSizeChanger: true,
          }}
        />
      </Card>

      {/* Process Modal */}
      <Modal
        title={
          <Space>
            <FileDoneOutlined style={{ color: '#F26F21' }} />
            <span>Phê duyệt đơn từ #{selectedRequest?.requestId}</span>
          </Space>
        }
        open={processModalOpen}
        onCancel={() => setProcessModalOpen(false)}
        footer={null}
        destroyOnClose
        width={560}
      >
        {selectedRequest && (
          <div style={{ marginBottom: 20 }}>
            <div
              style={{
                background: '#F9FAFB',
                padding: 16,
                borderRadius: 8,
                marginBottom: 16,
              }}
            >
              <div style={{ marginBottom: 8 }}>
                <Text type="secondary">Sinh viên nộp: </Text>
                <Text strong>{selectedRequest.studentName} ({selectedRequest.studentRollNumber})</Text>
              </div>
              <div style={{ marginBottom: 8 }}>
                <Text type="secondary">Loại đơn: </Text>
                {getRequestTypeTag(selectedRequest.requestType)}
              </div>
              <div style={{ marginBottom: 8 }}>
                <Text type="secondary">Tiêu đề: </Text>
                <Text strong>{selectedRequest.title}</Text>
              </div>
              <div>
                <Text type="secondary">Nội dung chi tiết: </Text>
                <Paragraph style={{ margin: '4px 0 0 0', color: '#262626' }}>
                  {selectedRequest.content}
                </Paragraph>
              </div>
              {selectedRequest.attachmentUrl && (
                <div style={{ marginTop: 8 }}>
                  <a href={selectedRequest.attachmentUrl} target="_blank" rel="noopener noreferrer">
                    <PaperClipOutlined /> Tệp minh chứng đính kèm
                  </a>
                </div>
              )}
            </div>

            <Form form={form} layout="vertical" onFinish={handleProcessSubmit}>
              <Form.Item
                name="status"
                label="Quyết định xử lý"
                rules={[{ required: true, message: 'Vui lòng chọn quyết định' }]}
              >
                <Radio.Group buttonStyle="solid">
                  <Radio.Button value="APPROVED" style={{ color: '#52C41A' }}>
                    <CheckCircleOutlined /> Phê duyệt (APPROVED)
                  </Radio.Button>
                  <Radio.Button value="REJECTED" style={{ color: '#F5222D' }}>
                    <CloseCircleOutlined /> Từ chối (REJECTED)
                  </Radio.Button>
                </Radio.Group>
              </Form.Item>

              <Form.Item name="responseNote" label="Ý kiến phản hồi cho sinh viên">
                <Input.TextArea rows={3} placeholder="Lý do từ chối hoặc thông tin hướng dẫn..." />
              </Form.Item>

              <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
                <Space>
                  <Button onClick={() => setProcessModalOpen(false)}>Đóng</Button>
                  <Button
                    type="primary"
                    htmlType="submit"
                    loading={processLoading}
                    style={{ background: '#F26F21', borderColor: '#F26F21' }}
                  >
                    Xác nhận
                  </Button>
                </Space>
              </Form.Item>
            </Form>
          </div>
        )}
      </Modal>
    </div>
  )
}

export default RequestManagementPage
