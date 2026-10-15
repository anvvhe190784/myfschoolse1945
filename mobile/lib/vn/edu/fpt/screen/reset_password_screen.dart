import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/reset_password_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/widget/bounce_tap.dart';

class ResetPasswordScreen extends StatefulWidget {
  final String email;
  final String otp;
  final String? resetToken;
  final ResetPasswordController? controller;

  const ResetPasswordScreen({
    super.key,
    this.email = 'example@email.com',
    this.otp = '',
    this.resetToken,
    this.controller,
  });

  @override
  State<ResetPasswordScreen> createState() => _ResetPasswordScreenState();
}

class _ResetPasswordScreenState extends State<ResetPasswordScreen> {
  late final ResetPasswordController _controller;
  late final bool _ownsController;

  static const Color primaryYellow = Color(0xFFF5A623);
  static const Color labelYellow = Color(0xFFD97706);
  static const Color textDark = Color(0xFF1E293B);

  @override
  void initState() {
    super.initState();
    _ownsController = widget.controller == null;
    _controller = widget.controller ??
        ResetPasswordController(
          email: widget.email,
          otp: widget.otp,
          resetToken: widget.resetToken,
        );
    _controller.addListener(_onControllerUpdate);
  }

  void _onControllerUpdate() {
    if (mounted) setState(() {});
  }

  @override
  void dispose() {
    _controller.removeListener(_onControllerUpdate);
    if (_ownsController) {
      _controller.dispose();
    }
    super.dispose();
  }

  void _handleResetPassword() async {
    final newPassword = _controller.newPasswordController.text;
    final confirmPassword = _controller.confirmPasswordController.text;

    final messenger = ScaffoldMessenger.of(context);
    messenger.hideCurrentSnackBar();

    if (newPassword.isEmpty || confirmPassword.isEmpty) {
      messenger.showSnackBar(
        const SnackBar(content: Text('Vui lòng điền cả 2 trường mật khẩu')),
      );
      return;
    }

    if (newPassword != confirmPassword) {
      messenger.showSnackBar(
        const SnackBar(content: Text('Mật khẩu xác nhận không khớp')),
      );
      return;
    }

    final success = await _controller.resetPassword();
    if (!mounted) return;

    if (!success) {
      if (_controller.errorMessage != null) {
        messenger.showSnackBar(
          SnackBar(content: Text(_controller.errorMessage!)),
        );
      }
      return;
    }

    // Success dialog & return to LoginScreen
    showDialog<void>(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          title: const Text(
            'Thành công',
            style: TextStyle(fontWeight: FontWeight.bold, color: textDark),
          ),
          content: const Text(
            'Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.',
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop(); // close dialog
                Navigator.of(context).popUntil((route) => route.isFirst); // back to login
              },
              child: const Text(
                'Đăng nhập ngay',
                style: TextStyle(color: labelYellow, fontWeight: FontWeight.bold),
              ),
            ),
          ],
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(28.0, 0.0, 28.0, 72.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                const SizedBox(height: 5),
                Image.asset(
                  'assets/images/fpt_logo.png',
                  height: 100,
                  fit: BoxFit.contain,
                ),
                const SizedBox(height: 48),

                // Header & Subtitle
                Align(
                  alignment: Alignment.centerLeft,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                       const Text(
                        'Đặt lại mật khẩu',
                        style: TextStyle(
                          fontSize: 28,
                          fontWeight: FontWeight.bold,
                          color: textDark,
                          letterSpacing: -0.5,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Email đã được xác thực thành công. Vui lòng\ntạo mật khẩu mới bên dưới.',
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey.shade600,
                          height: 1.4,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 36),

                // New Password Field
                const Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    'Mật khẩu mới',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: labelYellow,
                    ),
                  ),
                ),
                const SizedBox(height: 6),
                TextField(
                  controller: _controller.newPasswordController,
                  obscureText: _controller.obscureNewPassword,
                  decoration: InputDecoration(
                    hintText: '••••••••••••',
                    hintStyle: TextStyle(
                      color: Colors.grey.shade400,
                      fontSize: 15,
                    ),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(
                        color: Colors.grey.shade300,
                        width: 1.2,
                      ),
                    ),
                    focusedBorder: const UnderlineInputBorder(
                      borderSide: BorderSide(color: primaryYellow, width: 2.0),
                    ),
                    contentPadding: const EdgeInsets.symmetric(vertical: 10),
                    suffixIcon: IconButton(
                      icon: Icon(
                        _controller.obscureNewPassword
                            ? Icons.visibility
                            : Icons.visibility_off,
                        color: Colors.grey.shade500,
                        size: 22,
                      ),
                      onPressed: _controller.toggleObscureNewPassword,
                    ),
                  ),
                ),
                const SizedBox(height: 24),

                // Confirm Password Field
                const Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    'Xác nhận mật khẩu',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: labelYellow,
                    ),
                  ),
                ),
                const SizedBox(height: 6),
                TextField(
                  controller: _controller.confirmPasswordController,
                  obscureText: _controller.obscureConfirmPassword,
                  decoration: InputDecoration(
                    hintText: '••••••••••••',
                    hintStyle: TextStyle(
                      color: Colors.grey.shade400,
                      fontSize: 15,
                    ),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(
                        color: Colors.grey.shade300,
                        width: 1.2,
                      ),
                    ),
                    focusedBorder: const UnderlineInputBorder(
                      borderSide: BorderSide(color: primaryYellow, width: 2.0),
                    ),
                    contentPadding: const EdgeInsets.symmetric(vertical: 10),
                    suffixIcon: IconButton(
                      icon: Icon(
                        _controller.obscureConfirmPassword
                            ? Icons.visibility
                            : Icons.visibility_off,
                        color: Colors.grey.shade500,
                        size: 22,
                      ),
                      onPressed: _controller.toggleObscureConfirmPassword,
                    ),
                  ),
                ),
                const SizedBox(height: 36),

                // Submit Button
                BounceTap(
                  child: SizedBox(
                    width: double.infinity,
                    height: 52,
                    child: ElevatedButton(
                      onPressed: _handleResetPassword,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: primaryYellow,
                        foregroundColor: textDark,
                        elevation: 2,
                        shadowColor: primaryYellow.withValues(alpha: 0.4),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      child: _controller.isLoading
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: textDark,
                              ),
                            )
                          : const Text(
                              'Đặt lại mật khẩu',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                    ),
                  ),
                ),
                const SizedBox(height: 24),

                // Back to Sign In
                BounceTap(
                  child: TextButton(
                    onPressed: () {
                      Navigator.of(context).popUntil((route) => route.isFirst);
                    },
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          Icons.arrow_back_rounded,
                          size: 16,
                          color: Colors.grey.shade600,
                        ),
                        const SizedBox(width: 6),
                        Text(
                          'Quay lại đăng nhập',
                          style: TextStyle(
                            color: Colors.grey.shade600,
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
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
}
