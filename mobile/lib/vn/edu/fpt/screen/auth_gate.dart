import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/home_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/login_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

/// AuthGate kiểm tra phiên đăng nhập được lưu trong Secure Storage khi mở app.
/// - Nếu token hợp lệ: tự động hiển thị HomeScreen.
/// - Nếu không có token hoặc hết hạn: hiển thị LoginScreen.
class AuthGate extends StatefulWidget {
  final ApiClient? apiClient;

  const AuthGate({super.key, this.apiClient});

  @override
  State<AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends State<AuthGate> {
  late final ApiClient _apiClient;
  bool _checking = true;
  bool _isAuthenticated = false;

  @override
  void initState() {
    super.initState();
    _apiClient = widget.apiClient ?? ApiClient.instance;
    _checkSession();
  }

  Future<void> _checkSession() async {
    final token = await _apiClient.restoreSession();
    if (!mounted) return;
    setState(() {
      _isAuthenticated = token != null && token.isNotEmpty;
      _checking = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_checking) {
      return Scaffold(
        backgroundColor: Colors.white,
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Image.asset(
                'assets/images/fpt_logo.png',
                height: 130,
                fit: BoxFit.contain,
                errorBuilder: (_, _, _) => const Icon(
                  Icons.school_rounded,
                  size: 80,
                  color: Color(0xFFF5A623),
                ),
              ),
              const SizedBox(height: 28),
              const CircularProgressIndicator(
                color: Color(0xFFF5A623),
                strokeWidth: 2.5,
              ),
            ],
          ),
        ),
      );
    }

    if (_isAuthenticated) {
      return const HomeScreen();
    }

    return const LoginScreen();
  }
}
