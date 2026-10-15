import 'package:flutter/material.dart';

/// Notification model representing a notification item from the backend.
class NotificationModel {
  final int notificationId;
  final int? userId;
  final String title;
  final String content;
  final String type; // 'NEWS', 'SECURITY', 'ACADEMIC', 'REQUEST', 'SYSTEM'
  final int? targetId;
  final bool isRead;
  final DateTime createdAt;

  const NotificationModel({
    required this.notificationId,
    this.userId,
    required this.title,
    required this.content,
    required this.type,
    this.targetId,
    this.isRead = false,
    required this.createdAt,
  });

  factory NotificationModel.fromJson(Map<String, dynamic> json) {
    final rawCreatedAt = json['createdAt'] ?? json['created_at'];
    final parsedCreatedAt = rawCreatedAt != null ? DateTime.tryParse(rawCreatedAt.toString()) : null;
    return NotificationModel(
      notificationId: json['notificationId'] ?? json['notification_id'] ?? 0,
      userId: json['userId'] ?? json['user_id'],
      title: json['title'] ?? '',
      content: json['content'] ?? '',
      type: json['type'] ?? 'SYSTEM',
      targetId: json['targetId'] ?? json['target_id'],
      isRead: json['isRead'] ?? json['is_read'] ?? false,
      createdAt: parsedCreatedAt ?? DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'notificationId': notificationId,
      if (userId != null) 'userId': userId,
      'title': title,
      'content': content,
      'type': type,
      if (targetId != null) 'targetId': targetId,
      'isRead': isRead,
      'createdAt': createdAt.toIso8601String(),
    };
  }

  NotificationModel copyWith({
    int? notificationId,
    int? userId,
    String? title,
    String? content,
    String? type,
    int? targetId,
    bool? isRead,
    DateTime? createdAt,
  }) {
    return NotificationModel(
      notificationId: notificationId ?? this.notificationId,
      userId: userId ?? this.userId,
      title: title ?? this.title,
      content: content ?? this.content,
      type: type ?? this.type,
      targetId: targetId ?? this.targetId,
      isRead: isRead ?? this.isRead,
      createdAt: createdAt ?? this.createdAt,
    );
  }

  /// Visual icon based on notification type
  IconData get icon {
    switch (type.toUpperCase()) {
      case 'NEWS':
        return Icons.campaign_rounded;
      case 'SECURITY':
        return Icons.shield_outlined;
      case 'ACADEMIC':
        return Icons.school_outlined;
      case 'REQUEST':
        return Icons.description_outlined;
      default:
        return Icons.notifications_outlined;
    }
  }

  /// Theme color based on notification type
  Color get color {
    switch (type.toUpperCase()) {
      case 'NEWS':
        return const Color(0xFF2563EB); // Vibrant Blue
      case 'SECURITY':
        return const Color(0xFF059669); // Emerald Green
      case 'ACADEMIC':
        return const Color(0xFF7C3AED); // Deep Purple
      case 'REQUEST':
        return const Color(0xFFD97706); // Amber
      default:
        return const Color(0xFF6B7280); // Neutral Slate
    }
  }

  /// User-friendly label for notification category
  String get typeLabel {
    switch (type.toUpperCase()) {
      case 'NEWS':
        return 'Tin tức';
      case 'SECURITY':
        return 'Bảo mật';
      case 'ACADEMIC':
        return 'Học vụ';
      case 'REQUEST':
        return 'Đơn từ';
      default:
        return 'Thông báo';
    }
  }

  /// Relative human-readable time format
  String get formattedTime {
    final now = DateTime.now();
    final difference = now.difference(createdAt);

    if (difference.inMinutes < 1) {
      return 'Vừa xong';
    } else if (difference.inMinutes < 60) {
      return '${difference.inMinutes} phút trước';
    } else if (difference.inHours < 24) {
      return '${difference.inHours} giờ trước';
    } else if (difference.inDays < 7) {
      return '${difference.inDays} ngày trước';
    } else {
      final local = createdAt.toLocal();
      return '${local.day.toString().padLeft(2, '0')}/${local.month.toString().padLeft(2, '0')}/${local.year}';
    }
  }
}
