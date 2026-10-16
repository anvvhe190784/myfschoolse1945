import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';
import 'mock_api_client.dart';

void main() {
  group('Controllers Unit Tests', () {
    setUp(() {
      ApiClient.instance = MockApiClient();
    });

    tearDown(() {
      ApiClient.instance = null;
    });

    test('LoginController initial state, password toggle and validation', () async {
      final controller = LoginController();
      expect(controller.obscurePassword, isTrue);
      expect(controller.isLoading, isFalse);

      controller.togglePasswordVisibility();
      expect(controller.obscurePassword, isFalse);

      // Validation should fail on empty fields
      final err = controller.validate();
      expect(err, isNotNull);

      controller.emailController.text = 'student@fpt.edu.vn';
      controller.passwordController.text = 'secret123';
      expect(controller.validate(), isNull);

      // Test login with offline fallback
      final success = await controller.login(allowOfflineFallback: true);
      expect(success, isTrue);
      expect(controller.loginResponse, isNotNull);
      expect(controller.loginResponse?.email, 'student@fpt.edu.vn');
      controller.dispose();
    });

    test('LoginController fails validation and login on empty fields', () async {
      final controller = LoginController();
      final success = await controller.login();
      expect(success, isFalse);
      expect(controller.errorMessage, 'Vui lòng nhập Email hoặc Mã số');
      expect(controller.loginResponse, isNull);
      controller.dispose();
    });

    test('LoginController fails login when backend rejects invalid credentials', () async {
      final failingApiClient = FailingMockApiClient();
      final controller = LoginController(apiClient: failingApiClient);
      controller.emailController.text = 'student@fpt.edu.vn';
      controller.passwordController.text = 'wrongpassword';

      // Even if allowOfflineFallback was accidentally true, ApiException must NEVER be swallowed
      final success = await controller.login(allowOfflineFallback: true);
      expect(success, isFalse);
      expect(controller.errorMessage, 'Tài khoản hoặc mật khẩu không chính xác');
      expect(controller.loginResponse, isNull);
      controller.dispose();
    });

    test('ForgotPasswordController email validation and code sending', () async {
      final controller = ForgotPasswordController();
      controller.emailController.text = 'invalid-email';
      expect(controller.validate(), 'Định dạng email không hợp lệ');

      controller.emailController.text = 'valid@fpt.edu.vn';
      expect(controller.validate(), isNull);

      final result = await controller.sendVerificationCode();
      expect(result, isTrue);
      controller.dispose();
    });

    test('VerifyEmailController OTP logic and countdown timer', () async {
      final controller = VerifyEmailController(email: 'test@fpt.edu.vn');
      expect(controller.secondsRemaining, 45);
      expect(controller.canResend, isFalse);
      expect(controller.isOtpComplete, isFalse);

      // Fill in 6 digits
      for (int i = 0; i < 6; i++) {
        controller.controllers[i].text = '$i';
      }
      expect(controller.currentOtp, '012345');
      expect(controller.isOtpComplete, isTrue);

      final success = await controller.verifyOtp();
      expect(success, isTrue);
      controller.dispose();
    });

    test('ResetPasswordController password match and submission', () async {
      final controller = ResetPasswordController(email: 'test@fpt.edu.vn');
      expect(controller.obscureNewPassword, isTrue);
      expect(controller.obscureConfirmPassword, isTrue);

      controller.toggleObscureNewPassword();
      expect(controller.obscureNewPassword, isFalse);

      // Invalid password policy (missing special char)
      controller.newPasswordController.text = 'NewPass123';
      controller.confirmPasswordController.text = 'NewPass123';
      expect(controller.validate(),
          'Mật khẩu mới không thỏa mãn đầy đủ các điều kiện bảo mật.');

      // Valid password policy but mismatch
      controller.newPasswordController.text = 'NewPass123!';
      controller.confirmPasswordController.text = 'Mismatch123!';
      expect(controller.validate(), 'Mật khẩu xác nhận không khớp');

      // Valid and matching
      controller.confirmPasswordController.text = 'NewPass123!';
      expect(controller.validate(), isNull);

      final success = await controller.resetPassword(allowOfflineFallback: true);
      expect(success, isTrue);
      controller.dispose();
    });

    test('HomeController tab navigation and news feed', () async {
      final mockClient = MockApiClient();
      final controller = HomeController(apiClient: mockClient, studentName: 'Vũ Văn An');
      await controller.loadNews();
      expect(controller.selectedNavIndex, 0);
      expect(controller.studentName, 'Vũ Văn An');
      expect(controller.menuItems.length, 8);
      expect(controller.newsList.length, 3);
      expect(controller.newsList.first.title, 'Lich thi Hoc ky Fall 2026 chinh thuc');
      expect(controller.newsList.first.badgeText, 'HỌC VỤ');

      controller.setNavIndex(2);
      expect(controller.selectedNavIndex, 2);

      controller.updateStudentName('Nguyễn Văn B');
      expect(controller.studentName, 'Nguyễn Văn B');
      controller.dispose();
    });

    test('HomeController loadUserProfile updates studentName when authenticated', () async {
      final mockClient = MockApiClient();
      mockClient.setAuthToken('valid-token');
      final controller = HomeController(apiClient: mockClient, studentName: 'Học sinh');
      await controller.loadUserProfile();
      expect(controller.studentName, 'Mock User');
      controller.dispose();
    });
  });
}
