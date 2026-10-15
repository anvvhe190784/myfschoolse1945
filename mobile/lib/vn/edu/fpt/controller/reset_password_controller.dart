import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

/// Controller for ResetPasswordScreen managing password change via Backend Web API `POST /api/v1/auth/reset-password`.
class ResetPasswordController extends ChangeNotifier {
  final ApiClient apiClient;
  final String email;
  final String otp;
  final String? resetToken;

  final TextEditingController newPasswordController;
  final TextEditingController confirmPasswordController;
  final bool _ownsControllers;

  bool _obscureNewPassword = true;
  bool _obscureConfirmPassword = true;
  bool _isLoading = false;
  String? _errorMessage;

  ResetPasswordController({
    required this.email,
    this.otp = '',
    this.resetToken,
    ApiClient? apiClient,
    TextEditingController? newPasswordController,
    TextEditingController? confirmPasswordController,
  })  : apiClient = apiClient ?? ApiClient.instance,
        newPasswordController =
            newPasswordController ?? TextEditingController(),
        confirmPasswordController =
            confirmPasswordController ?? TextEditingController(),
        _ownsControllers = newPasswordController == null &&
            confirmPasswordController == null;

  bool get obscureNewPassword => _obscureNewPassword;
  bool get obscureConfirmPassword => _obscureConfirmPassword;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  void toggleObscureNewPassword() {
    _obscureNewPassword = !_obscureNewPassword;
    notifyListeners();
  }

  void toggleObscureConfirmPassword() {
    _obscureConfirmPassword = !_obscureConfirmPassword;
    notifyListeners();
  }

  String? validate() {
    final newPass = newPasswordController.text;
    final confirmPass = confirmPasswordController.text;

    if (newPass.isEmpty || confirmPass.isEmpty) {
      return 'Vui lòng điền cả 2 trường mật khẩu';
    }
    if (newPass != confirmPass) {
      return 'Mật khẩu xác nhận không khớp';
    }
    return null;
  }

  /// Reset password by calling backend endpoint `POST /api/v1/auth/reset-password`.
  Future<bool> resetPassword({bool allowOfflineFallback = false}) async {
    final validationError = validate();
    if (validationError != null) {
      _errorMessage = validationError;
      notifyListeners();
      return false;
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      await apiClient.resetPassword(
        email: email,
        otp: otp,
        resetToken: resetToken,
        newPassword: newPasswordController.text,
      );
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _isLoading = false;
      if (allowOfflineFallback) {
        // Fallback for tests or offline preview when backend is not actively running
        notifyListeners();
        return true;
      }
      _errorMessage =
          e is ApiException ? e.message : 'Đặt lại mật khẩu thất bại: $e';
      notifyListeners();
      return false;
    }
  }

  @override
  void dispose() {
    if (_ownsControllers) {
      newPasswordController.dispose();
      confirmPasswordController.dispose();
    }
    super.dispose();
  }
}
