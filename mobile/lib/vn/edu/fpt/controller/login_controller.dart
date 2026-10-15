import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/model/auth_dto.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

/// Controller for LoginScreen managing authentication state and API calls.
class LoginController extends ChangeNotifier {
  final ApiClient apiClient;

  final TextEditingController emailController;
  final TextEditingController passwordController;
  final bool _ownsControllers;

  bool _obscurePassword = true;
  bool _isLoading = false;
  String? _errorMessage;
  LoginResponse? _loginResponse;

  LoginController({
    ApiClient? apiClient,
    TextEditingController? emailController,
    TextEditingController? passwordController,
  })  : apiClient = apiClient ?? ApiClient.instance,
        emailController = emailController ?? TextEditingController(),
        passwordController = passwordController ?? TextEditingController(),
        _ownsControllers = emailController == null && passwordController == null;

  bool get obscurePassword => _obscurePassword;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  LoginResponse? get loginResponse => _loginResponse;

  void togglePasswordVisibility() {
    _obscurePassword = !_obscurePassword;
    notifyListeners();
  }

  void clearError() {
    _errorMessage = null;
    notifyListeners();
  }

  String? validate() {
    final email = emailController.text.trim();
    final password = passwordController.text;

    if (email.isEmpty) {
      return 'Vui lòng nhập Email hoặc Mã số';
    }
    if (password.isEmpty) {
      return 'Vui lòng nhập Mật khẩu';
    }
    return null;
  }

  /// Perform authentication via Backend Web API `POST /api/v1/auth/login`.
  /// Supports offline/mock fallback for widget tests if the backend server is unreachable.
  Future<bool> login({bool allowOfflineFallback = false}) async {
    final validationError = validate();
    if (validationError != null) {
      _errorMessage = validationError;
      notifyListeners();
      return false;
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    final identifier = emailController.text.trim();
    final password = passwordController.text;

    try {
      final json = await apiClient.login(
        identifier: identifier,
        password: password,
      );
      _loginResponse = LoginResponse.fromJson(json);
      _isLoading = false;
      notifyListeners();
      return true;
    } on ApiException catch (e) {
      _isLoading = false;
      _errorMessage = e.message;
      notifyListeners();
      return false;
    } catch (e) {
      _isLoading = false;
      if (allowOfflineFallback) {
        // Fallback for tests or offline preview when backend is not actively running
        _loginResponse = _createOfflineFallbackResponse(identifier);
        apiClient.setAuthToken('offline-jwt-token', 'offline-refresh-token');
        notifyListeners();
        return true;
      }
      _errorMessage = 'Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng hoặc thử lại sau.';
      notifyListeners();
      return false;
    }
  }

  LoginResponse _createOfflineFallbackResponse(String identifier) {
    String? rollNumber;
    String email;

    if (identifier.contains('@')) {
      rollNumber = null;
      email = identifier;
    } else if (identifier.isEmpty) {
      rollNumber = 'HE194500';
      email = 'anvvhe194500@fpt.edu.vn';
    } else {
      rollNumber = identifier;
      email = '$identifier@fpt.edu.vn';
    }

    return LoginResponse(
      accessToken: 'offline-jwt-token',
      refreshToken: 'offline-refresh-token',
      userId: 1,
      rollNumber: rollNumber,
      email: email,
      fullName: 'Vũ Văn An',
      role: 'STUDENT',
    );
  }

  @override
  void dispose() {
    if (_ownsControllers) {
      emailController.dispose();
      passwordController.dispose();
    }
    super.dispose();
  }
}
