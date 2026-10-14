import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/forgot_password_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/verify_email_screen.dart';

class ForgotPasswordScreen extends StatefulWidget {
  final ForgotPasswordController? controller;

  const ForgotPasswordScreen({super.key, this.controller});

  @override
  State<ForgotPasswordScreen> createState() => _ForgotPasswordScreenState();
}

class _ForgotPasswordScreenState extends State<ForgotPasswordScreen> {
  late final ForgotPasswordController _controller;
  late final bool _ownsController;

  static const Color primaryYellow = Color(0xFFF5A623);
  static const Color labelYellow = Color(0xFFD97706);
  static const Color textDark = Color(0xFF1E293B);

  @override
  void initState() {
    super.initState();
    _ownsController = widget.controller == null;
    _controller = widget.controller ?? ForgotPasswordController();
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

  void _handleSendVerificationEmail() async {
    final messenger = ScaffoldMessenger.of(context);
    messenger.hideCurrentSnackBar();

    final email = _controller.emailController.text.trim();
    if (email.isEmpty) {
      messenger.showSnackBar(
        const SnackBar(content: Text('Vui lòng nhập địa chỉ email của bạn')),
      );
      return;
    }

    final success = await _controller.sendVerificationCode();
    if (!mounted) return;

    if (success) {
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (context) => VerifyEmailScreen(email: email),
        ),
      );
    } else if (_controller.errorMessage != null) {
      messenger.showSnackBar(
        SnackBar(content: Text(_controller.errorMessage!)),
      );
    }
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
                const SizedBox(height: 60),

                // Forgot Password Header
                Align(
                  alignment: Alignment.centerLeft,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Quên mật khẩu?',
                        style: TextStyle(
                          fontSize: 28,
                          fontWeight: FontWeight.bold,
                          color: textDark,
                          letterSpacing: -0.5,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        "Nhập địa chỉ email của bạn để nhận\nmã xác thực đặt lại mật khẩu.",
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

                // Email Field
                Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    'Email',
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: labelYellow,
                    ),
                  ),
                ),
                const SizedBox(height: 6),
                TextField(
                  controller: _controller.emailController,
                  keyboardType: TextInputType.emailAddress,
                  decoration: InputDecoration(
                    hintText: 'example@email.com',
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
                  ),
                ),
                const SizedBox(height: 36),

                // Send Button
                SizedBox(
                  width: double.infinity,
                  height: 52,
                  child: ElevatedButton(
                    onPressed: _handleSendVerificationEmail,
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
                            'Gửi mã xác nhận',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                  ),
                ),
                const SizedBox(height: 24),

                // Back to Sign In
                TextButton(
                  onPressed: () {
                    Navigator.of(context).pop();
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
              ],
            ),
          ),
        ),
      ),
    );
  }
}
