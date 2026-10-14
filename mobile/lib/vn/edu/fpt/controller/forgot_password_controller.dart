import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

/// Controller for ForgotPasswordScreen managing email verification requests.
class ForgotPasswordController extends ChangeNotifier {
  final ApiClient apiClient;
  final TextEditingController emailController;
  final bool _ownsController;

  bool _isLoading = false;
  String? _errorMessage;

  ForgotPasswordController({
    ApiClient? apiClient,
    TextEditingController? emailController,
  })  : apiClient = apiClient ?? ApiClient.instance,
        emailController = emailController ?? TextEditingController(),
        _ownsController = emailController == null;

  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  String? validate() {
    final email = emailController.text.trim();
    if (email.isEmpty) {
      return 'Vui lòng nhập địa chỉ email';
    }
    final emailRegex = RegExp(r'^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$');
    if (!emailRegex.hasMatch(email)) {
      return 'Định dạng email không hợp lệ';
    }
    return null;
  }

  Future<bool> sendVerificationCode() async {
    final error = validate();
    if (error != null) {
      _errorMessage = error;
      notifyListeners();
      return false;
    }

    final email = emailController.text.trim();
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      await apiClient.forgotPassword(email: email);
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _isLoading = false;
      _errorMessage =
          e is ApiException ? e.message : 'Không thể gửi mã xác nhận: $e';
      notifyListeners();
      return false;
    }
  }

  @override
  void dispose() {
    if (_ownsController) {
      emailController.dispose();
    }
    super.dispose();
  }
}
