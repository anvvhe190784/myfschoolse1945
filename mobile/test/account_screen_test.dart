import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/account_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/account_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/change_password_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/login_screen.dart';
import 'mock_api_client.dart';

void main() {
  group('AccountScreen Widget Tests', () {
    testWidgets(
        'Renders title Tài khoản, NO back button, and NO settings gear icon',
        (WidgetTester tester) async {
      final controller = AccountController();
      await tester.pumpWidget(
        MaterialApp(
          home: AccountScreen(controller: controller),
        ),
      );

      // Verify title "Tài khoản"
      expect(find.text('Tài khoản'), findsOneWidget);

      // Verify NO back button
      expect(find.byType(BackButton), findsNothing);
      expect(find.byIcon(Icons.arrow_back), findsNothing);
      expect(find.byIcon(Icons.arrow_back_ios), findsNothing);
      expect(find.byIcon(Icons.arrow_back_ios_new), findsNothing);
      expect(find.text('< Quay lại'), findsNothing);
      expect(find.text('Quay lại'), findsNothing);

      // Verify NO settings gear icon
      expect(find.byIcon(Icons.settings), findsNothing);
      expect(find.byIcon(Icons.settings_outlined), findsNothing);
    });

    testWidgets(
        'Student ID card renders avatar, name, and ONLY student ID (no class or branch)',
        (WidgetTester tester) async {
      final controller = AccountController(
        studentName: 'Vũ Văn An',
        studentId: 'HS194500',
      );
      await tester.pumpWidget(
        MaterialApp(
          home: AccountScreen(controller: controller),
        ),
      );

      // Student name and ID
      expect(find.text('Vũ Văn An'), findsOneWidget);
      expect(find.text('Mã học sinh: HS194500'), findsOneWidget);

      // Ensure class and school branch are NOT displayed on the card
      expect(find.textContaining('Lớp:'), findsNothing);
      expect(find.textContaining('Cơ sở:'), findsNothing);
      expect(find.textContaining('Trường THPT'), findsNothing);
    });

    testWidgets('Renders Thông tin chi tiết and Cài đặt & Bảo mật sections',
        (WidgetTester tester) async {
      final controller = AccountController();
      await tester.pumpWidget(
        MaterialApp(
          home: AccountScreen(controller: controller),
        ),
      );

      // Section titles
      expect(find.text('Thông tin chi tiết'), findsOneWidget);
      expect(find.text('Cài đặt & Bảo mật'), findsOneWidget);

      // Information fields
      expect(find.text('Email'), findsOneWidget);
      expect(find.text('anvvhe194500@fpt.edu.vn'), findsOneWidget);

      expect(find.text('Số điện thoại'), findsOneWidget);
      expect(find.text('0987 654 321'), findsOneWidget);

      expect(find.text('Mã sinh viên'), findsOneWidget);
      expect(find.text('HE194500'), findsOneWidget);

      expect(find.text('Ngày tạo'), findsOneWidget);
      expect(find.text('14/10/2026'), findsOneWidget);

      // Settings options
      expect(find.text('Đổi mật khẩu'), findsOneWidget);
      expect(find.text('Liên hệ nhà trường'), findsOneWidget);
    });

    testWidgets('Tapping Đổi mật khẩu navigates to ChangePasswordScreen',
        (WidgetTester tester) async {
      final controller = AccountController();
      await tester.pumpWidget(
        MaterialApp(
          home: AccountScreen(controller: controller),
        ),
      );

      final changePassFinder = find.text('Đổi mật khẩu');
      expect(changePassFinder, findsOneWidget);

      await tester.tap(changePassFinder);
      await tester.pumpAndSettle();

      expect(find.byType(ChangePasswordScreen), findsOneWidget);
    });

    testWidgets(
        'Tapping Đăng xuất calls controller.logout and redirects to LoginScreen',
        (WidgetTester tester) async {
      final apiClient = MockApiClient();
      apiClient.setAuthToken('logged-in-jwt-token');
      final controller = AccountController(apiClient: apiClient);

      await tester.pumpWidget(
        MaterialApp(
          home: AccountScreen(controller: controller),
        ),
      );

      final logoutBtn = find.text('Đăng xuất');
      expect(logoutBtn, findsOneWidget);

      await tester.ensureVisible(logoutBtn);
      await tester.pumpAndSettle();

      await tester.tap(logoutBtn);
      await tester.pumpAndSettle();

      expect(apiClient.authToken, isNull);
      expect(find.byType(LoginScreen), findsOneWidget);
    });

    testWidgets('Renders dynamic profile details when authenticated',
        (WidgetTester tester) async {
      final apiClient = MockApiClient();
      apiClient.setAuthToken('logged-in-jwt-token');
      final controller = AccountController(apiClient: apiClient);

      await tester.pumpWidget(
        MaterialApp(
          home: AccountScreen(controller: controller),
        ),
      );
      await tester.pumpAndSettle();

      // Verify dynamic profile fetched via loadProfile
      expect(find.text('Mock User'), findsOneWidget);
      expect(find.text('mock@fpt.edu.vn'), findsOneWidget);
      expect(find.text('0912345678'), findsOneWidget);
      expect(find.text('SE12345'), findsOneWidget);
    });
  });
}
