import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/model/news_model.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

class FeatureMenuItem {
  final String title;
  final IconData icon;
  final Color bgColor;

  const FeatureMenuItem({
    required this.title,
    required this.icon,
    required this.bgColor,
  });
}

class HomeNewsItem {
  final NewsModel model;
  final String title;
  final String date;
  final String badgeText;
  final Color headerColor;
  final IconData icon;

  const HomeNewsItem({
    required this.model,
    required this.title,
    required this.date,
    required this.badgeText,
    required this.headerColor,
    required this.icon,
  });

  factory HomeNewsItem.fromModel(NewsModel model) {
    String badge = 'TIN TỨC';
    Color color = const Color(0xFFFF8A65);
    IconData ic = Icons.campaign_rounded;

    final cat = model.category.toUpperCase();
    final lowerTitle = model.title.toLowerCase();

    if (cat == 'ACADEMIC' || lowerTitle.contains('lịch thi') || lowerTitle.contains('thi') || lowerTitle.contains('học vụ')) {
      badge = 'HỌC VỤ';
      color = const Color(0xFF2563EB);
      ic = Icons.menu_book_rounded;
    } else if (cat == 'SCHOLARSHIP' || lowerTitle.contains('học bổng')) {
      badge = 'HỌC BỔNG';
      color = const Color(0xFF10B981);
      ic = Icons.card_giftcard_rounded;
    } else if (cat == 'EVENT' || lowerTitle.contains('thể thao') || lowerTitle.contains('sports') || lowerTitle.contains('camp')) {
      badge = 'SỰ KIỆN';
      color = const Color(0xFFFF8A65);
      ic = Icons.directions_run_rounded;
    } else if (cat == 'ACTIVITY' || lowerTitle.contains('chào đón') || lowerTitle.contains('school')) {
      badge = 'TỰU TRƯỜNG';
      color = const Color(0xFFFFB74D);
      ic = Icons.celebration_rounded;
    }

    return HomeNewsItem(
      model: model,
      title: model.title,
      date: model.formattedDate.isNotEmpty ? model.formattedDate : '14/10/2026',
      badgeText: badge,
      headerColor: color,
      icon: ic,
    );
  }
}

/// Controller for HomeScreen managing navigation tabs, student profile, quick actions, and news feed.
class HomeController extends ChangeNotifier {
  final ApiClient apiClient;

  int _selectedNavIndex = 0;
  String _studentName;
  bool _isLoadingNews = false;
  String? _newsError;
  List<HomeNewsItem> _newsItems = [];
  bool _disposed = false;

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

  final List<FeatureMenuItem> _menuItems = const [
    FeatureMenuItem(
      title: 'Lịch học',
      icon: Icons.calendar_month_rounded,
      bgColor: Color(0xFF2563EB),
    ),
    FeatureMenuItem(
      title: 'Bảng điểm',
      icon: Icons.assignment_turned_in_rounded,
      bgColor: Color(0xFF10B981),
    ),
    FeatureMenuItem(
      title: 'Đơn từ',
      icon: Icons.description_rounded,
      bgColor: Color(0xFFF97316),
    ),
    FeatureMenuItem(
      title: 'Câu lạc bộ',
      icon: Icons.groups_rounded,
      bgColor: Color(0xFFEC4899),
    ),
    FeatureMenuItem(
      title: 'Tin tức',
      icon: Icons.campaign_rounded,
      bgColor: Color(0xFF06B6D4),
    ),
    FeatureMenuItem(
      title: 'Khóa học',
      icon: Icons.school_rounded,
      bgColor: Color(0xFF8B5CF6),
    ),
    FeatureMenuItem(
      title: 'Lớp học',
      icon: Icons.co_present_rounded,
      bgColor: Color(0xFFF59E0B),
    ),
    FeatureMenuItem(
      title: 'Khác',
      icon: Icons.more_horiz_rounded,
      bgColor: Color(0xFF64748B),
    ),
  ];

  HomeController({
    ApiClient? apiClient,
    this._studentName = 'Học sinh',
  })  : apiClient = apiClient ?? ApiClient.instance {
    loadNews();
    loadUserProfile();
    loadUnreadNotificationCount();
  }

  int _unreadNotificationCount = 0;

  int get selectedNavIndex => _selectedNavIndex;
  String get studentName => _studentName;
  bool get isLoadingNews => _isLoadingNews;
  String? get newsError => _newsError;
  List<HomeNewsItem> get newsItems => _newsItems;
  List<HomeNewsItem> get newsList => _newsItems;
  List<FeatureMenuItem> get menuItems => _menuItems;
  int get unreadNotificationCount => _unreadNotificationCount;

  void setUnreadNotificationCount(int count) {
    _unreadNotificationCount = count;
    notifyListeners();
  }

  Future<void> loadUnreadNotificationCount() async {
    if (!apiClient.isAuthenticated) return;
    try {
      final count = await apiClient.getUnreadNotificationCount();
      _unreadNotificationCount = count;
      notifyListeners();
    } catch (_) {}
  }

  void setNavIndex(int index) {
    _selectedNavIndex = index;
    notifyListeners();
  }

  void updateStudentName(String name) {
    _studentName = name;
    notifyListeners();
  }

  Future<void> loadUserProfile() async {
    if (!apiClient.isAuthenticated) return;
    try {
      final user = await apiClient.getCurrentUser();
      final name = user['fullName']?.toString();
      if (name != null && name.isNotEmpty) {
        updateStudentName(name);
      }
    } catch (_) {}
  }

  /// Fetch news from Backend Web API `GET /api/v1/news` from the database.
  Future<void> loadNews() async {
    _isLoadingNews = true;
    _newsError = null;
    notifyListeners();

    try {
      final list = await apiClient.getNews();
      _newsItems = list
          .map((item) =>
              HomeNewsItem.fromModel(NewsModel.fromJson(item as Map<String, dynamic>)))
          .toList();
      _isLoadingNews = false;
      notifyListeners();
    } catch (e) {
      _isLoadingNews = false;
      _newsError = e is ApiException ? e.message : 'Lỗi tải tin tức: $e';
      notifyListeners();
    }
  }
}
