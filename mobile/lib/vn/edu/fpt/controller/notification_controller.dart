import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/model/notification_model.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

enum NotificationFilter { all, unread }

/// Controller managing notification state, unread counts, and interactions.
class NotificationController extends ChangeNotifier {
  final ApiClient apiClient;

  bool _isLoading = false;
  String? _error;
  List<NotificationModel> _notifications = [];
  int _unreadCount = 0;
  NotificationFilter _currentFilter = NotificationFilter.all;
  bool _disposed = false;

  NotificationController({ApiClient? apiClient})
      : apiClient = apiClient ?? ApiClient.instance;

  @override
  void dispose() {
    _disposed = true;
    super.dispose();
  }

  @override
  void notifyListeners() {
    if (!_disposed) {
      super.notifyListeners();
    }
  }

  bool get isLoading => _isLoading;
  String? get error => _error;
  List<NotificationModel> get notifications => _notifications;
  int get unreadCount => _unreadCount;
  NotificationFilter get currentFilter => _currentFilter;

  List<NotificationModel> get filteredNotifications {
    if (_currentFilter == NotificationFilter.unread) {
      return _notifications.where((n) => !n.isRead).toList();
    }
    return _notifications;
  }

  void setFilter(NotificationFilter filter) {
    if (_currentFilter != filter) {
      _currentFilter = filter;
      notifyListeners();
    }
  }

  /// Load full list of notifications and recalculate unread count
  Future<void> loadNotifications() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final list = await apiClient.getNotifications();
      _notifications = list
          .map((item) => NotificationModel.fromJson(item as Map<String, dynamic>))
          .toList();
      _unreadCount = _notifications.where((n) => !n.isRead).length;
      _isLoading = false;
      notifyListeners();
    } catch (e) {
      _isLoading = false;
      _error = e is ApiException ? e.message : 'Lỗi tải thông báo: $e';
      notifyListeners();
    }
  }

  /// Lightweight fetch of unread count only (e.g. for Home screen badge)
  Future<void> loadUnreadCount() async {
    try {
      final count = await apiClient.getUnreadNotificationCount();
      _unreadCount = count;
      notifyListeners();
    } catch (_) {
      // Fallback: calculate from existing list if available
      _unreadCount = _notifications.where((n) => !n.isRead).length;
      notifyListeners();
    }
  }

  /// Mark a specific notification as read
  Future<void> markAsRead(int notificationId) async {
    final index = _notifications.indexWhere((n) => n.notificationId == notificationId);
    if (index == -1 || _notifications[index].isRead) return;

    // Optimistic UI update
    _notifications[index] = _notifications[index].copyWith(isRead: true);
    _unreadCount = _notifications.where((n) => !n.isRead).length;
    notifyListeners();

    try {
      await apiClient.markNotificationRead(notificationId);
    } catch (_) {
      // Keep optimistic update or re-fetch on failure
    }
  }

  /// Mark all notifications as read in a single batch
  Future<void> markAllAsRead() async {
    if (_unreadCount == 0) return;

    // Optimistic UI update
    _notifications = _notifications.map((n) => n.copyWith(isRead: true)).toList();
    _unreadCount = 0;
    notifyListeners();

    try {
      await apiClient.markAllNotificationsRead();
    } catch (_) {
      await loadNotifications();
    }
  }
}
