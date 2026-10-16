import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';
import 'package:myfschoolse1945/vn/edu/fpt/util/password_policy.dart';

/// Controller for ChangePasswordScreen managing form state, validation, and API submission.
class ChangePasswordController extends ChangeNotifier {
  final ApiClient apiClient;

  final TextEditingController oldPasswordController = TextEditingController();
  final TextEditingController newPasswordController = TextEditingController();
  final TextEditingController confirmPasswordController = TextEditingController();

  bool _obscureOldPassword = true;
  bool _obscureNewPassword = true;
  bool _obscureConfirmPassword = true;

  bool _isLoading = false;
  String? _errorMessage;
  String? _successMessage;

  ChangePasswordController({ApiClient? apiClient})
      : apiClient = apiClient ?? ApiClient.instance {
    oldPasswordController.addListener(_onTextChanged);
    newPasswordController.addListener(_onTextChanged);
    confirmPasswordController.addListener(_onTextChanged);
  }

  void _onTextChanged() {
    notifyListeners();
  }

  bool get obscureOldPassword => _obscureOldPassword;
  bool get obscureNewPassword => _obscureNewPassword;
  bool get obscureConfirmPassword => _obscureConfirmPassword;

  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  String? get successMessage => _successMessage;

  bool get isOldPasswordEntered => oldPasswordController.text.trim().isNotEmpty;
  bool get isNewPasswordValid =>
      PasswordPolicy.isValid(newPasswordController.text);
  bool get isSameAsOldPassword =>
      oldPasswordController.text.isNotEmpty &&
      newPasswordController.text.isNotEmpty &&
      oldPasswordController.text == newPasswordController.text;
  bool get isConfirmPasswordMatching =>
      confirmPasswordController.text.isNotEmpty &&
      confirmPasswordController.text == newPasswordController.text;

  bool get canSubmit =>
      isOldPasswordEntered &&
      isNewPasswordValid &&
      !isSameAsOldPassword &&
      isConfirmPasswordMatching &&
      !_isLoading;

  void toggleObscureOldPassword() {
    _obscureOldPassword = !_obscureOldPassword;
    notifyListeners();
  }

  void toggleObscureNewPassword() {
    _obscureNewPassword = !_obscureNewPassword;
    notifyListeners();
  }

  void toggleObscureConfirmPassword() {
    _obscureConfirmPassword = !_obscureConfirmPassword;
    notifyListeners();
  }

  void clearMessages() {
    _errorMessage = null;
    _successMessage = null;
    notifyListeners();
  }

  /// Validate form input and call Backend API POST /api/v1/auth/change-password.
  Future<bool> changePassword() async {
    final oldPassword = oldPasswordController.text;
    final newPassword = newPasswordController.text;
    final confirmPassword = confirmPasswordController.text;

    _errorMessage = null;
    _successMessage = null;

    if (oldPassword.trim().isEmpty) {
      _errorMessage = 'Vui lòng nhập mật khẩu hiện tại';
      notifyListeners();
      return false;
    }

    if (newPassword.trim().isEmpty) {
      _errorMessage = 'Vui lòng nhập mật khẩu mới';
      notifyListeners();
      return false;
    }

    if (!PasswordPolicy.isValid(newPassword)) {
      _errorMessage = 'Mật khẩu mới không thỏa mãn đầy đủ các điều kiện bảo mật.';
      notifyListeners();
      return false;
    }

    if (newPassword == oldPassword) {
      _errorMessage = 'Mật khẩu mới không được trùng với mật khẩu hiện tại';
      notifyListeners();
      return false;
    }

    if (confirmPassword.trim().isEmpty) {
      _errorMessage = 'Vui lòng nhập xác nhận mật khẩu mới';
      notifyListeners();
      return false;
    }

    if (newPassword != confirmPassword) {
      _errorMessage = 'Mật khẩu xác nhận không khớp';
      notifyListeners();
      return false;
    }

    _isLoading = true;
    notifyListeners();

    try {
      final res = await apiClient.changePassword(
        oldPassword: oldPassword,
        newPassword: newPassword,
      );
      _successMessage = res['message']?.toString() ?? 'Đổi mật khẩu thành công!';
      return true;
    } on ApiException catch (e) {
      _errorMessage = e.message;
      return false;
    } catch (_) {
      _errorMessage = 'Có lỗi xảy ra khi đổi mật khẩu. Vui lòng thử lại sau.';
      return false;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  @override
  void dispose() {
    oldPasswordController.removeListener(_onTextChanged);
    newPasswordController.removeListener(_onTextChanged);
    confirmPasswordController.removeListener(_onTextChanged);
    oldPasswordController.dispose();
    newPasswordController.dispose();
    confirmPasswordController.dispose();
    super.dispose();
  }
}
