import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/change_password_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/login_screen.dart';

/// Screen for authenticated users to change their password, matching the FPT Schools design.
class ChangePasswordScreen extends StatefulWidget {
  final ChangePasswordController? controller;

  const ChangePasswordScreen({super.key, this.controller});

  @override
  State<ChangePasswordScreen> createState() => _ChangePasswordScreenState();
}

class _ChangePasswordScreenState extends State<ChangePasswordScreen> {
  late final ChangePasswordController _controller;
  late final bool _ownsController;

  static const Color brandOrange = Color(0xFFD97706);
  static const Color buttonYellow = Color(0xFFEAA340);
  static const Color iconPeachBg = Color(0xFFFFF3E6);
  static const Color textDark = Color(0xFF1E293B);
  static const Color textMuted = Color(0xFF94A3B8);
  static const Color fieldBorder = Color(0xFFE2E8F0);

  @override
  void initState() {
    super.initState();
    _ownsController = widget.controller == null;
    _controller = widget.controller ?? ChangePasswordController();
    _controller.addListener(_onControllerChanged);
  }

  void _onControllerChanged() {
    if (mounted) setState(() {});
  }

  @override
  void dispose() {
    _controller.removeListener(_onControllerChanged);
    if (_ownsController) {
      _controller.dispose();
    }
    super.dispose();
  }

  Future<void> _handleSubmit() async {
    final messenger = ScaffoldMessenger.of(context);
    messenger.hideCurrentSnackBar();

    final success = await _controller.changePassword();
    if (!mounted) return;

    if (!success) {
      if (_controller.errorMessage != null) {
        messenger.showSnackBar(
          SnackBar(
            content: Text(_controller.errorMessage!),
            backgroundColor: const Color(0xFFEF4444),
          ),
        );
      }
      return;
    }

    messenger.showSnackBar(
      const SnackBar(
        content: Text('Đổi mật khẩu thành công! Vui lòng đăng nhập lại.'),
        backgroundColor: Color(0xFF16A34A),
      ),
    );

    // Xóa session cục bộ vì phiên cũ trên server đã bị hủy hoàn toàn
    await _controller.apiClient.clearAuthToken();
    if (!mounted) return;

    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 28.0, vertical: 24.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                // 1. FPT Logo & Brand Header
                Image.asset(
                  'assets/images/fpt_logo.png',
                  height: 64,
                  fit: BoxFit.contain,
                ),
                const SizedBox(height: 8),
                const Text(
                  'FPT SCHOOLS',
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.w900,
                    color: brandOrange,
                    letterSpacing: 1.2,
                  ),
                ),
                const SizedBox(height: 28),

                // 2. Circular Key & Lock Illustration
                Container(
                  width: 72,
                  height: 72,
                  decoration: const BoxDecoration(
                    color: iconPeachBg,
                    shape: BoxShape.circle,
                  ),
                  child: Center(
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        Transform.rotate(
                          angle: -0.5,
                          child: const Icon(
                            Icons.vpn_key_outlined,
                            size: 30,
                            color: brandOrange,
                          ),
                        ),
                        Positioned(
                          right: 18,
                          bottom: 16,
                          child: Container(
                            padding: const EdgeInsets.all(1.5),
                            decoration: const BoxDecoration(
                              color: iconPeachBg,
                              shape: BoxShape.circle,
                            ),
                            child: const Icon(
                              Icons.lock_outline_rounded,
                              size: 14,
                              color: brandOrange,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // 3. Screen Title
                const Text(
                  'Đổi mật khẩu',
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.bold,
                    color: textDark,
                    letterSpacing: -0.3,
                  ),
                ),
                const SizedBox(height: 24),

                // 4. Input Fields
                _buildPasswordField(
                  controller: _controller.oldPasswordController,
                  hintText: 'Mật khẩu hiện tại',
                  obscureText: _controller.obscureOldPassword,
                  onToggleObscure: _controller.toggleObscureOldPassword,
                ),
                const SizedBox(height: 14),

                _buildPasswordField(
                  controller: _controller.newPasswordController,
                  hintText: 'Mật khẩu mới',
                  obscureText: _controller.obscureNewPassword,
                  onToggleObscure: _controller.toggleObscureNewPassword,
                ),
                const SizedBox(height: 14),

                _buildPasswordField(
                  controller: _controller.confirmPasswordController,
                  hintText: 'Xác nhận mật khẩu mới',
                  obscureText: _controller.obscureConfirmPassword,
                  onToggleObscure: _controller.toggleObscureConfirmPassword,
                ),
                const SizedBox(height: 22),

                // 5. Submit Button
                SizedBox(
                  width: double.infinity,
                  height: 48,
                  child: ElevatedButton(
                    onPressed: _controller.isLoading ? null : _handleSubmit,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: buttonYellow,
                      foregroundColor: textDark,
                      elevation: 1,
                      shadowColor: buttonYellow.withValues(alpha: 0.3),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(10),
                      ),
                    ),
                    child: _controller.isLoading
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2.2,
                              color: textDark,
                            ),
                          )
                        : const Text(
                            'Đổi mật khẩu',
                            style: TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.bold,
                              letterSpacing: 0.2,
                            ),
                          ),
                  ),
                ),
                const SizedBox(height: 14),

                // 6. Back Button
                TextButton(
                  onPressed: () {
                    if (Navigator.of(context).canPop()) {
                      Navigator.of(context).pop();
                    }
                  },
                  style: TextButton.styleFrom(
                    foregroundColor: textMuted,
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  ),
                  child: const Text(
                    'Quay lại',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildPasswordField({
    required TextEditingController controller,
    required String hintText,
    required bool obscureText,
    required VoidCallback onToggleObscure,
  }) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: fieldBorder, width: 1.2),
      ),
      padding: const EdgeInsets.symmetric(horizontal: 14.0, vertical: 2.0),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: controller,
              obscureText: obscureText,
              style: const TextStyle(
                fontSize: 14,
                color: textDark,
                fontWeight: FontWeight.w500,
              ),
              decoration: InputDecoration(
                hintText: hintText,
                hintStyle: const TextStyle(
                  color: textMuted,
                  fontSize: 14,
                  fontWeight: FontWeight.normal,
                ),
                border: InputBorder.none,
                isDense: true,
                contentPadding: const EdgeInsets.symmetric(vertical: 12.0),
              ),
            ),
          ),
          IconButton(
            icon: Icon(
              obscureText ? Icons.visibility_off_outlined : Icons.visibility_outlined,
              color: const Color(0xFF64748B),
              size: 20,
            ),
            splashRadius: 18,
            onPressed: onToggleObscure,
          ),
        ],
      ),
    );
  }
}
