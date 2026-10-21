import React from 'react'
import { Modal, Descriptions, Avatar, Tag, Space, Typography } from 'antd'
import { UserOutlined, MailOutlined, PhoneOutlined, IdcardOutlined, SafetyCertificateOutlined } from '@ant-design/icons'
import { useAuth } from '@/contexts/AuthContext'

const { Title, Text } = Typography

interface ProfileModalProps {
  open: boolean
  onClose: () => void
}

export const ProfileModal: React.FC<ProfileModalProps> = ({ open, onClose }) => {
  const { user, role } = useAuth()

  if (!user) return null

  return (
    <Modal
      title={
        <Space align="center">
          <Avatar
            size={40}
            icon={<UserOutlined />}
            src={user.avatarUrl}
            style={{ backgroundColor: role === 'ADMIN' ? '#002766' : '#F26F21' }}
          />
          <div>
            <Title level={5} style={{ margin: 0 }}>
              {user.fullName}
            </Title>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {user.email}
            </Text>
          </div>
        </Space>
      }
      open={open}
      onOk={onClose}
      onCancel={onClose}
      footer={null}
      width={560}
    >
      <div style={{ marginTop: 20 }}>
        <Descriptions bordered column={1} size="small">
          <Descriptions.Item
            label={
              <Space>
                <IdcardOutlined /> Mã định danh / Roll Number
              </Space>
            }
          >
            <strong>{user.rollNumber || 'N/A'}</strong>
          </Descriptions.Item>

          <Descriptions.Item
            label={
              <Space>
                <MailOutlined /> Địa chỉ Email FPT
              </Space>
            }
          >
            {user.email}
          </Descriptions.Item>

          <Descriptions.Item
            label={
              <Space>
                <PhoneOutlined /> Số điện thoại liên hệ
              </Space>
            }
          >
            {user.phoneNumber || 'Chưa cập nhật'}
          </Descriptions.Item>

          <Descriptions.Item
            label={
              <Space>
                <SafetyCertificateOutlined /> Phân quyền hệ thống
              </Space>
            }
          >
            {role === 'ADMIN' ? (
              <Tag color="volcano" style={{ fontWeight: 600 }}>
                QUẢN TRỊ VIÊN HỆ THỐNG (ADMIN)
              </Tag>
            ) : (
              <Tag color="orange" style={{ fontWeight: 600 }}>
                GIẢNG VIÊN (LECTURER)
              </Tag>
            )}
          </Descriptions.Item>

          <Descriptions.Item label="Trạng thái tài khoản">
            <Tag color="success">HOẠT ĐỘNG (ACTIVE)</Tag>
          </Descriptions.Item>
        </Descriptions>
      </div>
    </Modal>
  )
}

export default ProfileModal
