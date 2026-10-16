import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/change_password_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/change_password_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/login_screen.dart';
import 'mock_api_client.dart';

void main() {
  group('ChangePasswordScreen & Controller Tests', () {
    testWidgets('Renders all UI components matching mockup design and real-time checklist',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      // 1. Brand header
      expect(find.byType(Image), findsOneWidget);
      final imageWidget = tester.widget<Image>(find.byType(Image));
      expect(
        (imageWidget.image as AssetImage).assetName,
        'assets/images/fpt_logo.png',
      );

      // 2. Key icon
      expect(find.byIcon(Icons.vpn_key_outlined), findsOneWidget);

      // 3. Screen title
      expect(find.text('Đổi mật khẩu'), findsNWidgets(2)); // Title & Button

      // 4. Input field placeholders
      expect(find.text('Mật khẩu hiện tại'), findsOneWidget);
      expect(find.text('Mật khẩu mới'), findsOneWidget);
      expect(find.text('Xác nhận mật khẩu mới'), findsOneWidget);

      // 5. Visibility toggle icons (3 eye icons)
      expect(find.byIcon(Icons.visibility_off_outlined), findsNWidgets(3));

      // 6. Real-time criteria checklist
      expect(find.text('Điều kiện mật khẩu:'), findsOneWidget);
      expect(find.text('Độ dài từ 8 - 12 ký tự'), findsOneWidget);
      expect(find.text('Ít nhất 1 chữ cái in hoa (A-Z)'), findsOneWidget);
      expect(find.text('Ít nhất 1 chữ cái in thường (a-z)'), findsOneWidget);
      expect(find.text('Ít nhất 1 chữ số (0-9)'), findsOneWidget);
      expect(find.text('Ít nhất 1 ký tự đặc biệt (!@#\$...)'), findsOneWidget);
      expect(find.text('Khác mật khẩu hiện tại'), findsOneWidget);

      // 7. Back button
      expect(find.text('Quay lại'), findsOneWidget);
    });

    testWidgets('Eye icons toggle password visibility for all 3 fields',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      final eyeIcons = find.byIcon(Icons.visibility_off_outlined);
      expect(eyeIcons, findsNWidgets(3));

      // Tap first eye icon
      await tester.ensureVisible(eyeIcons.at(0));
      await tester.tap(eyeIcons.at(0));
      await tester.pump();
      expect(find.byIcon(Icons.visibility_outlined), findsOneWidget);
      expect(find.byIcon(Icons.visibility_off_outlined), findsNWidgets(2));

      // Tap second eye icon
      final secondEye = find.byIcon(Icons.visibility_off_outlined).at(0);
      await tester.ensureVisible(secondEye);
      await tester.tap(secondEye);
      await tester.pump();
      expect(find.byIcon(Icons.visibility_outlined), findsNWidgets(2));
      expect(find.byIcon(Icons.visibility_off_outlined), findsOneWidget);

      // Tap third eye icon
      final thirdEye = find.byIcon(Icons.visibility_off_outlined).first;
      await tester.ensureVisible(thirdEye);
      await tester.tap(thirdEye);
      await tester.pump();
      expect(find.byIcon(Icons.visibility_outlined), findsNWidgets(3));
      expect(find.byIcon(Icons.visibility_off_outlined), findsNothing);
    });

    testWidgets('Submit button is disabled initially and when conditions are not satisfied',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      final submitBtn = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đổi mật khẩu'),
      );
      expect(submitBtn.onPressed, isNull);

      // Enter old password only -> still disabled
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu hiện tại'), 'OldPass123!');
      await tester.pump();
      final submitBtnAfterOld = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đổi mật khẩu'),
      );
      expect(submitBtnAfterOld.onPressed, isNull);

      // Enter weak new password -> still disabled
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'abc');
      await tester.pump();
      final submitBtnWeak = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đổi mật khẩu'),
      );
      expect(submitBtnWeak.onPressed, isNull);

      // Enter new password matching old password -> checklist shows error, warning text shown, button disabled
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'OldPass123!');
      await tester.enterText(
          find.widgetWithText(TextField, 'Xác nhận mật khẩu mới'), 'OldPass123!');
      await tester.pump();
      final submitBtnSame = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đổi mật khẩu'),
      );
      expect(submitBtnSame.onPressed, isNull);
      // Red cancel icon for "Khác mật khẩu hiện tại"
      expect(find.byIcon(Icons.cancel_outlined), findsOneWidget);
      expect(
        find.text('Mật khẩu mới không được trùng với mật khẩu hiện tại'),
        findsOneWidget,
      );
    });

    testWidgets('Real-time validation updates checklist and unlocks button when all satisfied',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      // All 6 check items initially neutral gray (Icons.radio_button_unchecked)
      expect(find.byIcon(Icons.radio_button_unchecked), findsNWidgets(6));
      expect(find.byIcon(Icons.check_circle_rounded), findsNothing);
      expect(find.byIcon(Icons.cancel_outlined), findsNothing);

      // Enter old password
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu hiện tại'), 'OldPass123!');
      await tester.pump();

      // Enter weak new password (lowercase only, different from old) -> 2 met (lowercase, diff from old), 4 unmet in red
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'abc');
      await tester.pump();
      expect(find.byIcon(Icons.check_circle_rounded), findsNWidgets(2));
      expect(find.byIcon(Icons.cancel_outlined), findsNWidgets(4));

      // Enter new password matching old password -> warning appears
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'OldPass123!');
      await tester.pump();
      expect(
        find.text('Mật khẩu mới không được trùng với mật khẩu hiện tại'),
        findsOneWidget,
      );

      // Change new password to valid distinct password -> warning disappears and all 6 satisfied
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'NewPass123!');
      await tester.pump();
      expect(
        find.text('Mật khẩu mới không được trùng với mật khẩu hiện tại'),
        findsNothing,
      );
      expect(find.byIcon(Icons.check_circle_rounded), findsNWidgets(6));
      expect(find.byIcon(Icons.radio_button_unchecked), findsNothing);
      expect(find.byIcon(Icons.cancel_outlined), findsNothing);

      // Button is still disabled because confirm password is not entered yet
      var submitBtn = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đổi mật khẩu'),
      );
      expect(submitBtn.onPressed, isNull);
      expect(find.text('Mật khẩu xác nhận không trùng khớp'), findsNothing);

      // Enter mismatched confirm password -> warning text appears, button remains disabled
      await tester.enterText(
          find.widgetWithText(TextField, 'Xác nhận mật khẩu mới'), 'Different123!');
      await tester.pump();
      expect(find.text('Mật khẩu xác nhận không trùng khớp'), findsOneWidget);
      submitBtn = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đổi mật khẩu'),
      );
      expect(submitBtn.onPressed, isNull);

      // Enter matching confirm password -> warning disappears, button unlocked!
      await tester.enterText(
          find.widgetWithText(TextField, 'Xác nhận mật khẩu mới'), 'NewPass123!');
      await tester.pump();
      expect(find.text('Mật khẩu xác nhận không trùng khớp'), findsNothing);

      submitBtn = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đổi mật khẩu'),
      );
      expect(submitBtn.onPressed, isNotNull);
    });

    test('Controller direct validation handles edge cases', () async {
      final controller = ChangePasswordController();

      // 1. Empty old password
      expect(await controller.changePassword(), isFalse);
      expect(controller.errorMessage, 'Vui lòng nhập mật khẩu hiện tại');

      // 2. Empty new password
      controller.oldPasswordController.text = 'OldPass123!';
      expect(await controller.changePassword(), isFalse);
      expect(controller.errorMessage, 'Vui lòng nhập mật khẩu mới');

      // 3. New password missing special character
      controller.newPasswordController.text = 'NewPass123';
      expect(await controller.changePassword(), isFalse);
      expect(controller.errorMessage,
          'Mật khẩu mới không thỏa mãn đầy đủ các điều kiện bảo mật.');

      // 4. New password identical to old password
      controller.newPasswordController.text = 'OldPass123!';
      expect(await controller.changePassword(), isFalse);
      expect(controller.errorMessage,
          'Mật khẩu mới không được trùng với mật khẩu hiện tại');

      // 5. Empty confirm password
      controller.newPasswordController.text = 'NewPass123!';
      expect(await controller.changePassword(), isFalse);
      expect(controller.errorMessage, 'Vui lòng nhập xác nhận mật khẩu mới');

      // 6. Confirm password mismatch
      controller.confirmPasswordController.text = 'WrongPass123!';
      expect(await controller.changePassword(), isFalse);
      expect(controller.errorMessage, 'Mật khẩu xác nhận không khớp');

      controller.dispose();
    });

    testWidgets('Successful password change clears session and redirects to LoginScreen',
        (WidgetTester tester) async {
      final mockApi = MockApiClient();
      mockApi.setAuthToken('test-access-token');
      final controller = ChangePasswordController(apiClient: mockApi);

      await tester.pumpWidget(
        MaterialApp(
          home: ChangePasswordScreen(controller: controller),
        ),
      );

      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu hiện tại'), 'OldPass123!');
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'NewPass123!');
      await tester.enterText(
          find.widgetWithText(TextField, 'Xác nhận mật khẩu mới'), 'NewPass123!');
      await tester.pump();

      final submitBtn = find.widgetWithText(ElevatedButton, 'Đổi mật khẩu');
      await tester.ensureVisible(submitBtn);
      await tester.tap(submitBtn);
      await tester.pumpAndSettle();

      // Verify token cleared
      expect(mockApi.authToken, isNull);

      // Verify redirected to LoginScreen
      expect(find.byType(LoginScreen), findsOneWidget);
    });

    testWidgets('Backend error displays error SnackBar',
        (WidgetTester tester) async {
      final mockApi = MockApiClient();
      mockApi.changePasswordShouldSucceed = false;
      mockApi.changePasswordErrorMessage = 'Mật khẩu hiện tại không chính xác';
      final controller = ChangePasswordController(apiClient: mockApi);

      await tester.pumpWidget(
        MaterialApp(
          home: ChangePasswordScreen(controller: controller),
        ),
      );

      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu hiện tại'), 'WrongPass123!');
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'NewPass123!');
      await tester.enterText(
          find.widgetWithText(TextField, 'Xác nhận mật khẩu mới'), 'NewPass123!');
      await tester.pump();

      final submitBtn = find.widgetWithText(ElevatedButton, 'Đổi mật khẩu');
      await tester.ensureVisible(submitBtn);
      await tester.tap(submitBtn);
      await tester.pump();

      expect(find.text('Mật khẩu hiện tại không chính xác'), findsOneWidget);
    });

    testWidgets('Tapping Quay lại pops the route', (WidgetTester tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: Builder(
              builder: (context) => ElevatedButton(
                onPressed: () => Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const ChangePasswordScreen()),
                ),
                child: const Text('Go'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Go'));
      await tester.pumpAndSettle();
      expect(find.byType(ChangePasswordScreen), findsOneWidget);

      final backFinder = find.text('Quay lại');
      await tester.ensureVisible(backFinder);
      await tester.tap(backFinder);
      await tester.pumpAndSettle();
      expect(find.byType(ChangePasswordScreen), findsNothing);
    });
  });
}
