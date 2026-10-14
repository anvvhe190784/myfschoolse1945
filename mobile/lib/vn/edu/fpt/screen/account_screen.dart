import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/account_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/login_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/change_password_screen.dart';

/// Screen displaying student account profile, details, settings, and logout option.
class AccountScreen extends StatefulWidget {
  final AccountController? controller;

  const AccountScreen({
    super.key,
    this.controller,
  });

  @override
  State<AccountScreen> createState() => _AccountScreenState();
}

class _AccountScreenState extends State<AccountScreen> {
  late final AccountController _controller;
  late final bool _ownsController;

  static const Color primaryOrange = Color(0xFFF97316);
  static const Color textDark = Color(0xFF1E293B);
  static const Color textMuted = Color(0xFF64748B);
  static const Color cardBg = Colors.white;
  static const Color logoutRed = Color(0xFFEF4444);

  @override
  void initState() {
    super.initState();
    _ownsController = widget.controller == null;
    _controller = widget.controller ?? AccountController();
    _controller.loadProfile();
  }

  @override
  void dispose() {
    if (_ownsController) {
      _controller.dispose();
    }
    super.dispose();
  }

  Future<void> _handleLogout() async {
    await _controller.logout();
    if (!mounted) return;

    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return ListenableBuilder(
      listenable: _controller,
      builder: (context, _) {
        return Scaffold(
          backgroundColor: const Color(0xFFF8FAFC),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0.5,
        centerTitle: true,
        automaticallyImplyLeading: false, // NO back button
        title: const Text(
          'Tài khoản',
          style: TextStyle(
            color: textDark,
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
        actions: const [], // NO settings gear icon
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _buildStudentCard(),
              const SizedBox(height: 20),
              _buildSectionTitle('Thông tin chi tiết'),
              const SizedBox(height: 10),
              _buildInfoSection(),
              const SizedBox(height: 20),
              _buildSectionTitle('Cài đặt & Bảo mật'),
              const SizedBox(height: 10),
              _buildSettingsSection(),
              const SizedBox(height: 28),
              _buildLogoutButton(),
              const SizedBox(height: 20),
            ],
          ),
        ),
      ),
    );
      },
    );
  }

  Widget _buildStudentCard() {
    return Container(
      padding: const EdgeInsets.all(20.0),
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
            color: primaryOrange.withValues(alpha: 0.35),
            blurRadius: 16,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(3.0),
            decoration: const BoxDecoration(
              color: Colors.white,
              shape: BoxShape.circle,
            ),
            child: const CircleAvatar(
              radius: 32,
              backgroundColor: Color(0xFFFFEDD5),
              child: Icon(
                Icons.person_rounded,
                color: Color(0xFFEA580C),
                size: 40,
              ),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  _controller.studentName,
                  style: const TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 20,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 6),
                Container(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 4,
                  ),
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.22),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Text(
                    'Mã học sinh: ${_controller.studentId}',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      letterSpacing: 0.3,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: const TextStyle(
        fontSize: 16,
        fontWeight: FontWeight.bold,
        color: textDark,
      ),
    );
  }

  Widget _buildInfoSection() {
    return Container(
      decoration: BoxDecoration(
        color: cardBg,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.grey.shade200),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.03),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        children: [
          _buildInfoRow(
            icon: Icons.email_outlined,
            iconColor: const Color(0xFF3B82F6),
            label: 'Email',
            value: _controller.email,
          ),
          _buildDivider(),
          _buildInfoRow(
            icon: Icons.phone_outlined,
            iconColor: const Color(0xFF10B981),
            label: 'Số điện thoại',
            value: _controller.phone,
          ),
          _buildDivider(),
          _buildInfoRow(
            icon: Icons.badge_outlined,
            iconColor: const Color(0xFF8B5CF6),
            label: 'Mã sinh viên',
            value: _controller.rollNumber,
          ),
          _buildDivider(),
          _buildInfoRow(
            icon: Icons.calendar_today_outlined,
            iconColor: const Color(0xFFF59E0B),
            label: 'Ngày tạo',
            value: _controller.createdDate,
          ),
        ],
      ),
    );
  }

  Widget _buildInfoRow({
    required IconData icon,
    required Color iconColor,
    required String label,
    required String value,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 14.0),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: iconColor.withValues(alpha: 0.12),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(icon, color: iconColor, size: 20),
          ),
          const SizedBox(width: 14),
          Text(
            label,
            style: const TextStyle(
              fontSize: 14,
              color: textMuted,
              fontWeight: FontWeight.w500,
            ),
          ),
          const Spacer(),
          Text(
            value,
            style: const TextStyle(
              fontSize: 14,
              color: textDark,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSettingsSection() {
    return Container(
      decoration: BoxDecoration(
        color: cardBg,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.grey.shade200),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.03),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        children: [
          _buildSettingsActionRow(
            icon: Icons.lock_outline_rounded,
            iconColor: primaryOrange,
            title: 'Đổi mật khẩu',
            onTap: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => const ChangePasswordScreen(),
                ),
              );
            },
          ),
          _buildDivider(),
          _buildSettingsActionRow(
            icon: Icons.headset_mic_outlined,
            iconColor: const Color(0xFF06B6D4),
            title: 'Liên hệ nhà trường',
            onTap: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                  content: Text('Hotline nhà trường: 1900 633 678'),
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _buildSettingsActionRow({
    required IconData icon,
    required Color iconColor,
    required String title,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 14.0),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: iconColor.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(icon, color: iconColor, size: 20),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Text(
                title,
                style: const TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                  color: textDark,
                ),
              ),
            ),
            const Icon(
              Icons.chevron_right_rounded,
              color: Color(0xFF94A3B8),
              size: 22,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDivider() {
    return Divider(
      height: 1,
      thickness: 1,
      color: Colors.grey.shade100,
      indent: 16,
      endIndent: 16,
    );
  }

  Widget _buildLogoutButton() {
    return SizedBox(
      height: 52,
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: const Color(0xFFFEF2F2),
          foregroundColor: logoutRed,
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(14),
            side: const BorderSide(color: Color(0xFFFECACA), width: 1.2),
          ),
        ),
        onPressed: _controller.isLoggingOut ? null : _handleLogout,
        child: _controller.isLoggingOut
            ? const SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  color: logoutRed,
                ),
              )
            : const Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.logout_rounded, color: logoutRed, size: 20),
                  SizedBox(width: 8),
                  Text(
                    'Đăng xuất',
                    style: TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.bold,
                      color: logoutRed,
                    ),
                  ),
                ],
              ),
      ),
    );
  }
}
