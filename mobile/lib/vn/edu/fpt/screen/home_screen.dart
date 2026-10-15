import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/account_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/home_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/account_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/notification_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/widget/bounce_tap.dart';


class HomeScreen extends StatefulWidget {
  final String studentName;
  final String? email;
  final String? rollNumber;
  final HomeController? controller;
  final AccountController? accountController;

  const HomeScreen({
    super.key,
    this.studentName = 'Học sinh',
    this.email,
    this.rollNumber,
    this.controller,
    this.accountController,
  });

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  late final HomeController _controller;
  late final bool _ownsController;
  late final AccountController _accountController;
  late final bool _ownsAccountController;

  static const Color primaryOrange = Color(0xFFF97316);
  static const Color textDark = Color(0xFF1E293B);

  @override
  void initState() {
    super.initState();
    _ownsController = widget.controller == null;
    _controller = widget.controller ??
        HomeController(studentName: widget.studentName);

    _ownsAccountController = widget.accountController == null;
    _accountController = widget.accountController ??
        AccountController(
          studentName: widget.studentName,
          email: widget.email ?? 'anvvhe194500@fpt.edu.vn',
          rollNumber: widget.rollNumber ?? 'HE194500',
          studentId: widget.rollNumber ?? 'HS194500',
        );
    _accountController.loadProfile();
  }

  @override
  void dispose() {
    if (_ownsController) {
      _controller.dispose();
    }
    if (_ownsAccountController) {
      _accountController.dispose();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _controller,
      builder: (context, _) {
        return Scaffold(
          backgroundColor: Colors.white,
          body: _buildBody(),
          bottomNavigationBar: BottomNavigationBar(
            currentIndex: _controller.selectedNavIndex,
            onTap: (index) {
              _controller.setNavIndex(index);
            },

            type: BottomNavigationBarType.fixed,
            backgroundColor: Colors.white,
            selectedItemColor: primaryOrange,
            unselectedItemColor: const Color(0xFF94A3B8),
            selectedFontSize: 12,
            unselectedFontSize: 12,
            elevation: 8,
            items: const [
              BottomNavigationBarItem(
                icon: Icon(Icons.home_rounded),
                label: 'Trang chủ',
              ),
              BottomNavigationBarItem(
                icon: Icon(Icons.chat_bubble_outline_rounded),
                label: 'Tin nhắn',
              ),
              BottomNavigationBarItem(
                icon: Icon(Icons.notifications_none_rounded),
                label: 'Hoạt động',
              ),
              BottomNavigationBarItem(
                icon: Icon(Icons.person_outline_rounded),
                label: 'Tài khoản',
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildBody() {
    switch (_controller.selectedNavIndex) {
      case 0:
        return _buildHomeContent();
      case 1:
        return _buildPlaceholderTab(
          title: 'Tin nhắn',
          icon: Icons.chat_bubble_outline_rounded,
          description: 'Hộp thư trao đổi giữa giảng viên và sinh viên',
        );
      case 2:
        return _buildPlaceholderTab(
          title: 'Hoạt động',
          icon: Icons.notifications_none_rounded,
          description: 'Lịch sử điểm danh và sự kiện ngoại khóa',
        );
      case 3:
        return AccountScreen(
          controller: _accountController,
        );
      default:
        return _buildHomeContent();
    }
  }

  Widget _buildPlaceholderTab({
    required String title,
    required IconData icon,
    required String description,
  }) {
    return SafeArea(
      child: Center(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 32.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, size: 72, color: primaryOrange),
              const SizedBox(height: 16),
              Text(
                title,
                style: const TextStyle(
                  fontSize: 22,
                  fontWeight: FontWeight.bold,
                  color: textDark,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                description,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 14,
                  color: Color(0xFF64748B),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHomeContent() {
    return SafeArea(
      child: RefreshIndicator(
        color: primaryOrange,
        onRefresh: () async {
          await _controller.loadNews();
          await _controller.loadUserProfile();
        },
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildTopBar(),
              const SizedBox(height: 12),
              _buildGreetingBanner(),
              const SizedBox(height: 24),
              _buildQuickActionGrid(),
              const SizedBox(height: 24),
              _buildNewsSection(),
              const SizedBox(height: 28),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTopBar() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 8.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Image.asset(
            'assets/images/fpt_logo.png',
            height: 38,
            fit: BoxFit.contain,
          ),
          Row(
            children: [
              Stack(
                clipBehavior: Clip.none,
                children: [
                  IconButton(
                    icon: const Icon(
                      Icons.notifications_none_rounded,
                      color: textDark,
                      size: 26,
                    ),
                    onPressed: () async {
                      await Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => const NotificationScreen(),
                        ),
                      );
                      _controller.loadUnreadNotificationCount();
                    },
                  ),
                  if (_controller.unreadNotificationCount > 0)
                    Positioned(
                      top: 6,
                      right: 6,
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 2),
                        constraints: const BoxConstraints(
                          minWidth: 16,
                          minHeight: 16,
                        ),
                        decoration: BoxDecoration(
                          color: const Color(0xFFEF4444),
                          borderRadius: BorderRadius.circular(10),
                          border: Border.all(color: Colors.white, width: 1.5),
                        ),
                        child: Text(
                          _controller.unreadNotificationCount > 9
                              ? '9+'
                              : '${_controller.unreadNotificationCount}',
                          textAlign: TextAlign.center,
                          style: const TextStyle(
                            color: Colors.white,
                            fontSize: 9,
                            fontWeight: FontWeight.bold,
                            height: 1,
                          ),
                        ),
                      ),
                    ),
                ],
              ),
              IconButton(
                icon: const Icon(
                  Icons.search_rounded,
                  color: textDark,
                  size: 26,
                ),
                onPressed: () {},
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildGreetingBanner() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 20.0),
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 16.0),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        gradient: const LinearGradient(
          colors: [
            Color(0xFFF97316),
            Color(0xFFFB923C),
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFFF97316).withValues(alpha: 0.35),
            blurRadius: 15,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(2.5),
            decoration: const BoxDecoration(
              color: Colors.white,
              shape: BoxShape.circle,
            ),
            child: const CircleAvatar(
              radius: 26,
              backgroundColor: Color(0xFFFFEDD5),
              child: Icon(
                Icons.person_rounded,
                color: Color(0xFFEA580C),
                size: 32,
              ),
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  'Chào buổi sáng, ${_controller.studentName}!',
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 4),
                Text(
                  'Chúc bạn một ngày học tập vui vẻ!',
                  style: TextStyle(
                    color: Colors.white.withValues(alpha: 0.9),
                    fontSize: 13,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildQuickActionGrid() {
    final menuItems = _controller.menuItems;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20.0),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: menuItems.sublist(0, 4).map(_buildMenuItem).toList(),
          ),
          const SizedBox(height: 18),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: menuItems.sublist(4, 8).map(_buildMenuItem).toList(),
          ),
        ],
      ),
    );
  }

  Widget _buildMenuItem(FeatureMenuItem item) {
    return BounceTap(
      onTap: () {
        ScaffoldMessenger.of(context).hideCurrentSnackBar();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Tính năng "${item.title}" đang được hoàn thiện.'),
            duration: const Duration(seconds: 2),
            behavior: SnackBarBehavior.floating,
          ),
        );
      },
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4.0, vertical: 2.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 58,
              height: 58,
              decoration: BoxDecoration(
                color: item.bgColor.withValues(alpha: 0.14),
                borderRadius: BorderRadius.circular(18),
              ),
              child: Icon(
                item.icon,
                color: item.bgColor,
                size: 28,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              item.title,
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w500,
                color: textDark,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildNewsSection() {
    final newsList = _controller.newsItems;
    final isLoading = _controller.isLoadingNews;

    Widget content;
    if (isLoading && newsList.isEmpty) {
      content = SizedBox(
        height: 205,
        child: ListView.separated(
          padding: const EdgeInsets.symmetric(horizontal: 20.0),
          scrollDirection: Axis.horizontal,
          itemCount: 3,
          separatorBuilder: (context, _) => const SizedBox(width: 14),
          itemBuilder: (context, index) => _buildNewsSkeleton(),
        ),
      );
    } else if (newsList.isEmpty) {
      content = Container(
        margin: const EdgeInsets.symmetric(horizontal: 20.0),
        padding: const EdgeInsets.symmetric(vertical: 28.0, horizontal: 20.0),
        decoration: BoxDecoration(
          color: const Color(0xFFF8FAFC),
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.grey.shade200),
        ),
        child: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.feed_outlined, size: 36, color: Colors.grey.shade400),
              const SizedBox(height: 8),
              Text(
                'Chưa có tin tức mới',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: Colors.grey.shade600,
                ),
              ),
              const SizedBox(height: 8),
              TextButton.icon(
                onPressed: () => _controller.loadNews(),
                icon: const Icon(Icons.refresh_rounded, size: 16),
                label: const Text('Tải lại'),
                style: TextButton.styleFrom(
                  foregroundColor: primaryOrange,
                  textStyle: const TextStyle(fontSize: 13, fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
        ),
      );
    } else {
      content = SizedBox(
        height: 205,
        child: ListView.separated(
          padding: const EdgeInsets.symmetric(horizontal: 20.0),
          scrollDirection: Axis.horizontal,
          itemCount: newsList.length,
          separatorBuilder: (context, _) => const SizedBox(width: 14),
          itemBuilder: (context, index) {
            final news = newsList[index];
            return BounceTap(
              onTap: () {
                ScaffoldMessenger.of(context).hideCurrentSnackBar();
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text('Xem chi tiết: ${news.title}'),
                    duration: const Duration(seconds: 2),
                    behavior: SnackBarBehavior.floating,
                  ),
                );
              },
              child: Container(
                width: 230,
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: Colors.grey.shade200, width: 1.0),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.04),
                      blurRadius: 10,
                      offset: const Offset(0, 4),
                    ),
                  ],
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      height: 85,
                      decoration: BoxDecoration(
                        color: news.headerColor.withValues(alpha: 0.12),
                        borderRadius: const BorderRadius.only(
                          topLeft: Radius.circular(16),
                          topRight: Radius.circular(16),
                        ),
                      ),
                      child: Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(
                              news.icon,
                              size: 32,
                              color: news.headerColor,
                            ),
                            const SizedBox(height: 4),
                            Text(
                              news.badgeText,
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.bold,
                                color: news.headerColor,
                                letterSpacing: 0.5,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                    Padding(
                      padding: const EdgeInsets.all(12.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            news.title,
                            style: const TextStyle(
                              fontSize: 13.5,
                              fontWeight: FontWeight.bold,
                              color: textDark,
                              height: 1.3,
                            ),
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                          ),
                          const SizedBox(height: 8),
                          Text(
                            news.date,
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.grey.shade500,
                              fontWeight: FontWeight.w400,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Tin tức & Sự kiện',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: textDark,
                ),
              ),
              GestureDetector(
                onTap: () {},
                child: const Text(
                  'Xem tất cả',
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                    color: primaryOrange,
                  ),
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 14),
        content,
      ],
    );
  }

  Widget _buildNewsSkeleton() {
    return Container(
      width: 230,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.grey.shade200),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            height: 85,
            decoration: const BoxDecoration(
              color: Color(0xFFF1F5F9),
              borderRadius: BorderRadius.only(
                topLeft: Radius.circular(16),
                topRight: Radius.circular(16),
              ),
            ),
            child: const Center(
              child: SizedBox(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  color: primaryOrange,
                ),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(12.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  width: double.infinity,
                  height: 14,
                  decoration: BoxDecoration(
                    color: Colors.grey.shade200,
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
                const SizedBox(height: 8),
                Container(
                  width: 110,
                  height: 12,
                  decoration: BoxDecoration(
                    color: Colors.grey.shade200,
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
