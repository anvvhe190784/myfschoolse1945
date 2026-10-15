import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/login_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/forgot_password_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/home_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/widget/bounce_tap.dart';

class LoginScreen extends StatefulWidget {
  final LoginController? controller;

  const LoginScreen({super.key, this.controller});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  late final LoginController _controller;
  late final bool _ownsController;

  // Primary yellow accent color
  static const Color primaryYellow = Color(0xFFF5A623);
  static const Color labelYellow = Color(0xFFD97706);

  @override
  void initState() {
    super.initState();
    _ownsController = widget.controller == null;
    _controller = widget.controller ?? LoginController();
  }

  @override
  void dispose() {
    if (_ownsController) {
      _controller.dispose();
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
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(
              28.0,
              0.0,
              28.0,
              72.0,
            ),
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

                // Sign In Header
                Align(
                  alignment: Alignment.centerLeft,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Đăng nhập',
                        style: TextStyle(
                          fontSize: 28,
                          fontWeight: FontWeight.bold,
                          color: Color(0xFF1E293B),
                          letterSpacing: -0.5,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Xin chào! Rất vui được gặp lại bạn.',
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey.shade600,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 36),

                // Email Field
                const Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    'Email',
                    style: TextStyle(
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
                const SizedBox(height: 24),

                // Password Field
                const Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    'Mật khẩu',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: labelYellow,
                    ),
                  ),
                ),
                const SizedBox(height: 6),
                TextField(
                  controller: _controller.passwordController,
                  obscureText: _controller.obscurePassword,
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
                        _controller.obscurePassword
                            ? Icons.visibility
                            : Icons.visibility_off,
                        color: Colors.grey.shade500,
                        size: 22,
                      ),
                      onPressed: _controller.togglePasswordVisibility,
                    ),
                  ),
                ),
                const SizedBox(height: 36),

                // Sign in Button
                BounceTap(
                  child: SizedBox(
                    width: double.infinity,
                    height: 52,
                    child: ElevatedButton(
                      onPressed: () async {
                        final success = await _controller.login();
                        if (!context.mounted) return;
                        if (success) {
                          final studentName =
                              _controller.loginResponse?.fullName ?? 'Sinh viên F-School';
                          final email = _controller.loginResponse?.email;
                          final rollNumber = _controller.loginResponse?.rollNumber;
                          Navigator.of(context).pushReplacement(
                            MaterialPageRoute(
                              builder: (context) => HomeScreen(
                                studentName: studentName,
                                email: email,
                                rollNumber: rollNumber,
                              ),
                            ),
                          );
                        } else if (_controller.errorMessage != null) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            SnackBar(
                              content: Text(_controller.errorMessage!),
                              backgroundColor: Colors.red.shade700,
                            ),
                          );
                        }
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: primaryYellow,
                        foregroundColor: const Color(0xFF1E293B),
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
                                color: Color(0xFF1E293B),
                              ),
                            )
                          : const Text(
                              'Đăng nhập',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                    ),
                  ),
                ),
                const SizedBox(height: 20),

                // Forgot Password Button
                BounceTap(
                  child: TextButton(
                    onPressed: () {
                      Navigator.of(context).push(
                        MaterialPageRoute(
                          builder: (context) => const ForgotPasswordScreen(),
                        ),
                      );
                    },
                    child: Text(
                      'Quên mật khẩu?',
                      style: TextStyle(
                        color: Colors.grey.shade600,
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
      },
    );
  }
}
