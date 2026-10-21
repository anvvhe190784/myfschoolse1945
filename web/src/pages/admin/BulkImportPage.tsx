import React, { useState } from 'react'
import {
  Card,
  Tabs,
  Button,
  Upload,
  Typography,
  Table,
  Space,
  Alert,
  Spin,
  Tag,
  message,
} from 'antd'
import type { UploadProps } from 'antd'
import {
  InboxOutlined,
  DownloadOutlined,
  UploadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  FileTextOutlined,
} from '@ant-design/icons'
import apiClient from '@/api/client'
import { ImportResult } from '@/types'

const { Title, Text, Paragraph } = Typography
const { Dragger } = Upload

type ImportType = 'users' | 'grades' | 'schedules' | 'enrollments'

interface TabConfig {
  key: ImportType
  title: string
  description: string
  sampleHeaders: string[]
}

const TAB_CONFIGS: TabConfig[] = [
  {
    key: 'users',
    title: 'Nạp Người Dùng (Users)',
    description: 'Import hàng loạt tài khoản sinh viên, giảng viên và cán bộ quản trị kèm thông tin liên hệ',
    sampleHeaders: ['rollNumber', 'email', 'fullName', 'phoneNumber', 'role', 'password'],
  },
  {
    key: 'grades',
    title: 'Nạp Điểm Số (Grades)',
    description: 'Import danh sách điểm thành phần (Lab, Quiz, PE, FE) theo mã lớp và mã sinh viên',
    sampleHeaders: ['classCode', 'rollNumber', 'assessmentName', 'weight', 'score', 'feedback'],
  },
  {
    key: 'schedules',
    title: 'Nạp Lịch Học (Schedules)',
    description: 'Import thời khóa biểu toàn trường bao gồm slot học, ngày học, phòng học và giảng viên',
    sampleHeaders: ['classCode', 'slotNumber', 'sessionNo', 'date', 'startTime', 'endTime', 'room', 'lecturerEmail'],
  },
  {
    key: 'enrollments',
    title: 'Xếp Lớp (Enrollments)',
    description: 'Import danh sách sinh viên đăng ký vào các lớp học phần trong học kỳ',
    sampleHeaders: ['classCode', 'rollNumber', 'status'],
  },
]

export const BulkImportPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<ImportType>('users')
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [previewHeaders, setPreviewHeaders] = useState<string[]>([])
  const [previewRows, setPreviewRows] = useState<Record<string, string>[]>([])
  const [importing, setImporting] = useState(false)
  const [importResult, setImportResult] = useState<ImportResult | null>(null)

  const currentTabConfig = TAB_CONFIGS.find((t) => t.key === activeTab)!

  // Handle Tab Change
  const handleTabChange = (key: string) => {
    setActiveTab(key as ImportType)
    setSelectedFile(null)
    setPreviewHeaders([])
    setPreviewRows([])
    setImportResult(null)
  }

  // Download CSV Template
  const handleDownloadTemplate = async () => {
    try {
      const response = await apiClient.get<string>(`/admin/import/templates/${activeTab}`, {
        responseType: 'blob',
      })
      const blob = new Blob([response.data as unknown as BlobPart], { type: 'text/csv;charset=utf-8;' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `${activeTab}_template.csv`)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)
      message.success(`Tải file mẫu ${activeTab}_template.csv thành công!`)
    } catch {
      message.error('Không thể tải file mẫu. Vui lòng thử lại!')
    }
  }

  // Handle CSV file selection and browser preview parsing
  const handleFileChange = (file: File) => {
    setSelectedFile(file)
    setImportResult(null)

    const reader = new FileReader()
    reader.onload = (e) => {
      const text = e.target?.result as string
      if (!text) return

      const lines = text.split(/\r\n|\n/).filter((l) => l.trim().length > 0)
      if (lines.length > 0) {
        const headers = lines[0].split(',').map((h) => h.trim().replace(/^"|"$/g, ''))
        setPreviewHeaders(headers)

        const rows = lines.slice(1, 11).map((line, idx) => {
          const values = line.split(',').map((v) => v.trim().replace(/^"|"$/g, ''))
          const obj: Record<string, string> = { key: String(idx) }
          headers.forEach((h, i) => {
            obj[h] = values[i] || ''
          })
          return obj
        })
        setPreviewRows(rows)
      }
    }
    reader.readAsText(file, 'utf-8')
    return false // prevent automatic upload
  }

  // Submit batch import
  const handleUploadSubmit = async () => {
    if (!selectedFile) {
      message.warning('Vui lòng chọn hoặc kéo thả tệp CSV trước khi nạp!')
      return
    }

    setImporting(true)
    setImportResult(null)

    try {
      const formData = new FormData()
      formData.append('file', selectedFile)

      const res = await apiClient.post<ImportResult>(`/admin/import/${activeTab}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })

      setImportResult(res.data)
      message.success(`Nạp dữ liệu ${activeTab} thành công!`)
    } catch (err: unknown) {
      const msg =
        (err as { rfcMessage?: string })?.rfcMessage || 'Nạp dữ liệu thất bại. Vui lòng kiểm tra định dạng CSV!'
      message.error(msg)
    } finally {
      setImporting(false)
    }
  }

  const uploadProps: UploadProps = {
    accept: '.csv',
    multiple: false,
    showUploadList: false,
    beforeUpload: (file) => {
      handleFileChange(file)
      return false
    },
  }

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto' }}>
      {/* Header */}
      <div style={{ marginBottom: 20 }}>
        <Title level={3} style={{ margin: 0, color: '#002766' }}>
          <UploadOutlined style={{ color: '#F26F21', marginRight: 10 }} />
          Nạp Dữ Liệu Hàng Loạt (Bulk CSV Import)
        </Title>
        <Text type="secondary">
          Tải về file mẫu chuẩn, tải lên dữ liệu CSV để tự động xử lý hàng loạt vào hệ thống F-School
        </Text>
      </div>

      <Card
        style={{ borderRadius: 12, boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}
        styles={{ body: { padding: '24px 32px' } }}
      >
        <Tabs
          activeKey={activeTab}
          onChange={handleTabChange}
          type="card"
          items={TAB_CONFIGS.map((t) => ({
            key: t.key,
            label: (
              <span style={{ fontWeight: 600 }}>
                <FileTextOutlined /> {t.title}
              </span>
            ),
          }))}
        />

        {/* Tab Description & Template Download */}
        <div
          style={{
            background: '#F9FAFB',
            padding: 20,
            borderRadius: 8,
            marginBottom: 24,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: 16,
          }}
        >
          <div>
            <Title level={5} style={{ margin: 0, color: '#002766' }}>
              {currentTabConfig.title}
            </Title>
            <Paragraph style={{ margin: '4px 0 0 0', color: '#595959', fontSize: 13 }}>
              {currentTabConfig.description}
            </Paragraph>
          </div>

          <Button
            type="primary"
            icon={<DownloadOutlined />}
            onClick={handleDownloadTemplate}
            style={{ background: '#002766', borderColor: '#002766', fontWeight: 600 }}
          >
            Tải file mẫu CSV
          </Button>
        </div>

        {/* Dragger Zone */}
        <div style={{ marginBottom: 24 }}>
          <Dragger {...uploadProps} style={{ padding: '20px 0', borderRadius: 10, background: '#fafafa' }}>
            <p className="ant-upload-drag-icon">
              <InboxOutlined style={{ color: '#F26F21', fontSize: 48 }} />
            </p>
            <p className="ant-upload-text" style={{ fontSize: 16, fontWeight: 600 }}>
              Kéo thả file CSV vào đây, hoặc click để chọn tệp
            </p>
            <p className="ant-upload-hint" style={{ color: '#8c8c8c' }}>
              Chỉ chấp nhận định dạng tệp <strong>.csv</strong> với mã hóa UTF-8. Tối đa 10,000 dòng mỗi lượt nạp.
            </p>
          </Dragger>

          {selectedFile && (
            <div
              style={{
                marginTop: 12,
                padding: '8px 16px',
                background: '#E6F7FF',
                borderRadius: 6,
                border: '1px solid #91D5FF',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <Text strong style={{ color: '#0050B3' }}>
                <FileTextOutlined style={{ marginRight: 6 }} /> Đã chọn file: {selectedFile.name} (
                {(selectedFile.size / 1024).toFixed(1)} KB)
              </Text>
              <Button size="small" type="link" danger onClick={() => setSelectedFile(null)}>
                Xóa file
              </Button>
            </div>
          )}
        </div>

        {/* CSV Preview Table */}
        {previewRows.length > 0 && (
          <div style={{ marginBottom: 24 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
              <Text strong>Xem trước dữ liệu ({previewRows.length} dòng đầu tiên):</Text>
              <Tag color="blue">Preview Mode</Tag>
            </div>
            <Table
              dataSource={previewRows}
              columns={previewHeaders.map((h) => ({
                title: h,
                dataIndex: h,
                key: h,
                render: (val: string) => <span style={{ fontSize: 13 }}>{val}</span>,
              }))}
              pagination={false}
              size="small"
              bordered
              scroll={{ x: 'max-content' }}
            />
          </div>
        )}

        {/* Action Button */}
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Button
            type="primary"
            size="large"
            icon={<UploadOutlined />}
            loading={importing}
            disabled={!selectedFile}
            onClick={handleUploadSubmit}
            style={{
              minWidth: 200,
              height: 48,
              fontSize: 16,
              fontWeight: 600,
              background: 'linear-gradient(135deg, #F26F21 0%, #FA8C16 100%)',
              borderColor: '#F26F21',
            }}
          >
            Bắt đầu nạp dữ liệu
          </Button>
        </div>

        {/* Import Results Report */}
        {importResult && (
          <Alert
            message={
              <Space>
                {importResult.failureCount === 0 ? (
                  <CheckCircleOutlined style={{ color: '#52C41A', fontSize: 18 }} />
                ) : (
                  <CloseCircleOutlined style={{ color: '#F5222D', fontSize: 18 }} />
                )}
                <span style={{ fontWeight: 700, fontSize: 15 }}>Kết quả nạp dữ liệu:</span>
              </Space>
            }
            description={
              <div style={{ marginTop: 8 }}>
                <Space size="large" style={{ marginBottom: 12 }}>
                  <Text>Tổng số dòng: <strong>{importResult.total}</strong></Text>
                  <Text style={{ color: '#52C41A' }}>
                    Thành công: <strong>{importResult.successCount}</strong>
                  </Text>
                  <Text style={{ color: '#F5222D' }}>
                    Thất bại: <strong>{importResult.failureCount}</strong>
                  </Text>
                </Space>

                {importResult.errors && importResult.errors.length > 0 && (
                  <div
                    style={{
                      background: '#FFF2F0',
                      border: '1px solid #FFCCC7',
                      borderRadius: 6,
                      padding: 12,
                      marginTop: 8,
                      maxHeight: 180,
                      overflowY: 'auto',
                    }}
                  >
                    <Text strong style={{ color: '#CF1322', display: 'block', marginBottom: 4 }}>
                      Chi tiết lỗi ghi nhận:
                    </Text>
                    <ul style={{ margin: 0, paddingLeft: 20, color: '#A8071A', fontSize: 13 }}>
                      {importResult.errors.map((err, idx) => (
                        <li key={idx}>{err}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            }
            type={importResult.failureCount === 0 ? 'success' : 'warning'}
            showIcon={false}
            style={{ borderRadius: 8 }}
          />
        )}
      </Card>
    </div>
  )
}

export default BulkImportPage
