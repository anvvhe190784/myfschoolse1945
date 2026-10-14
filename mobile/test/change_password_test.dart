import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/change_password_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/change_password_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/login_screen.dart';
import 'mock_api_client.dart';

void main() {
  group('ChangePasswordScreen & Controller Tests', () {
    testWidgets('Renders all UI components matching mockup design',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      // 1. Brand header
      expect(find.byType(Image), findsOneWidget);
      expect(find.text('FPT SCHOOLS'), findsOneWidget);

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

      // 6. Back button
      expect(find.text('Quay lại'), findsOneWidget);
    });

    testWidgets('Eye icons toggle password visibility for all 3 fields',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      // Find all 3 eye icon buttons
      final eyeIcons = find.byIcon(Icons.visibility_off_outlined);
      expect(eyeIcons, findsNWidgets(3));

      // Tap first eye icon -> toggles old password visibility
      await tester.tap(eyeIcons.at(0));
      await tester.pump();
      expect(find.byIcon(Icons.visibility_outlined), findsOneWidget);
      expect(find.byIcon(Icons.visibility_off_outlined), findsNWidgets(2));

      // Tap second eye icon -> toggles new password visibility
      await tester.tap(find.byIcon(Icons.visibility_off_outlined).at(0));
      await tester.pump();
      expect(find.byIcon(Icons.visibility_outlined), findsNWidgets(2));
      expect(find.byIcon(Icons.visibility_off_outlined), findsOneWidget);

      // Tap third eye icon -> toggles confirm password visibility
      await tester.tap(find.byIcon(Icons.visibility_off_outlined).first);
      await tester.pump();
      expect(find.byIcon(Icons.visibility_outlined), findsNWidgets(3));
      expect(find.byIcon(Icons.visibility_off_outlined), findsNothing);
    });

    testWidgets('Validation: Empty current password shows error SnackBar',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      // Tap submit with empty fields
      final submitBtn = find.widgetWithText(ElevatedButton, 'Đổi mật khẩu');
      await tester.tap(submitBtn);
      await tester.pump();

      expect(find.text('Vui lòng nhập mật khẩu hiện tại'), findsOneWidget);
    });

    testWidgets('Validation: Empty new password shows error SnackBar',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu hiện tại'), 'OldPass123');
      final submitBtn = find.widgetWithText(ElevatedButton, 'Đổi mật khẩu');
      await tester.tap(submitBtn);
      await tester.pump();

      expect(find.text('Vui lòng nhập mật khẩu mới'), findsOneWidget);
    });

    testWidgets('Validation: New password < 6 chars shows error SnackBar',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu hiện tại'), 'OldPass123');
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), '12345');
      final submitBtn = find.widgetWithText(ElevatedButton, 'Đổi mật khẩu');
      await tester.tap(submitBtn);
      await tester.pump();

      expect(find.text('Mật khẩu mới phải từ 6 ký tự trở lên'), findsOneWidget);
    });

    testWidgets('Validation: Confirm password mismatch shows error SnackBar',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu hiện tại'), 'OldPass123');
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'NewPass123');
      await tester.enterText(
          find.widgetWithText(TextField, 'Xác nhận mật khẩu mới'), 'MismatchPass');
      final submitBtn = find.widgetWithText(ElevatedButton, 'Đổi mật khẩu');
      await tester.tap(submitBtn);
      await tester.pump();

      expect(find.text('Mật khẩu xác nhận không khớp'), findsOneWidget);
    });

    testWidgets('Validation: New password same as old password shows error',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ChangePasswordScreen(),
        ),
      );

      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu hiện tại'), 'OldPass123');
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'OldPass123');
      await tester.enterText(
          find.widgetWithText(TextField, 'Xác nhận mật khẩu mới'), 'OldPass123');
      final submitBtn = find.widgetWithText(ElevatedButton, 'Đổi mật khẩu');
      await tester.tap(submitBtn);
      await tester.pump();

      expect(find.text('Mật khẩu mới không được trùng với mật khẩu hiện tại'),
          findsOneWidget);
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
          find.widgetWithText(TextField, 'Mật khẩu hiện tại'), 'OldPass123');
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'NewPass456');
      await tester.enterText(
          find.widgetWithText(TextField, 'Xác nhận mật khẩu mới'), 'NewPass456');

      final submitBtn = find.widgetWithText(ElevatedButton, 'Đổi mật khẩu');
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
          find.widgetWithText(TextField, 'Mật khẩu hiện tại'), 'WrongPass');
      await tester.enterText(
          find.widgetWithText(TextField, 'Mật khẩu mới'), 'NewPass456');
      await tester.enterText(
          find.widgetWithText(TextField, 'Xác nhận mật khẩu mới'), 'NewPass456');

      final submitBtn = find.widgetWithText(ElevatedButton, 'Đổi mật khẩu');
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

      await tester.tap(find.text('Quay lại'));
      await tester.pumpAndSettle();
      expect(find.byType(ChangePasswordScreen), findsNothing);
    });
  });
}
