import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/login_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/home_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/login_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';
import 'mock_api_client.dart';

void main() {
  testWidgets('LoginScreen renders FPT logo image, fields, yellow elements and handles password toggle', (WidgetTester tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: LoginScreen(),
      ),
    );

    // Verify FPT Logo Image presence
    expect(find.byType(Image), findsOneWidget);
    final imageWidget = tester.widget<Image>(find.byType(Image));
    expect((imageWidget.image as AssetImage).assetName, 'assets/images/fpt_logo.png');

    // Verify Header and subtitle
    expect(find.text('Đăng nhập'), findsNWidgets(2)); // Header và Button
    expect(find.text('Xin chào! Rất vui được gặp lại bạn.'), findsOneWidget);

    // Verify Email and Password labels
    expect(find.text('Email'), findsOneWidget);
    expect(find.text('Mật khẩu'), findsOneWidget);

    // Verify Sign in button & Forgot Password
    expect(find.widgetWithText(ElevatedButton, 'Đăng nhập'), findsOneWidget);
    expect(find.text('Quên mật khẩu?'), findsOneWidget);

    // Verify password visibility toggle
    final visibilityIcon = find.byIcon(Icons.visibility);
    expect(visibilityIcon, findsOneWidget);

    await tester.tap(visibilityIcon);
    await tester.pump();

    expect(find.byIcon(Icons.visibility_off), findsOneWidget);
  });

  testWidgets('LoginScreen displays error SnackBar when submitting empty fields', (WidgetTester tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: LoginScreen(),
      ),
    );

    await tester.tap(find.widgetWithText(ElevatedButton, 'Đăng nhập'));
    await tester.pump();

    expect(find.byType(SnackBar), findsOneWidget);
    expect(find.text('Vui lòng nhập Email hoặc Mã số'), findsOneWidget);
    expect(find.byType(LoginScreen), findsOneWidget);
  });

  testWidgets('LoginScreen displays error SnackBar when backend rejects credentials', (WidgetTester tester) async {
    final failingClient = FailingMockApiClient(
      errorMessage: 'Tài khoản hoặc mật khẩu không chính xác',
    );
    final controller = LoginController(apiClient: failingClient);

    await tester.pumpWidget(
      MaterialApp(
        home: LoginScreen(controller: controller),
      ),
    );

    await tester.enterText(find.byType(TextField).at(0), 'student@fpt.edu.vn');
    await tester.enterText(find.byType(TextField).at(1), 'wrongpass');

    await tester.tap(find.widgetWithText(ElevatedButton, 'Đăng nhập'));
    await tester.pump();

    expect(find.byType(SnackBar), findsOneWidget);
    expect(find.text('Tài khoản hoặc mật khẩu không chính xác'), findsOneWidget);
    expect(find.byType(LoginScreen), findsOneWidget);
  });

  testWidgets('LoginScreen sign in navigates to HomeScreen on valid credentials', (WidgetTester tester) async {
    ApiClient.instance = MockApiClient();

    await tester.pumpWidget(
      const MaterialApp(
        home: LoginScreen(),
      ),
    );

    // Enter valid credentials
    await tester.enterText(find.byType(TextField).at(0), 'student@fpt.edu.vn');
    await tester.enterText(find.byType(TextField).at(1), '123456');

    final signInBtn = find.widgetWithText(ElevatedButton, 'Đăng nhập');
    expect(signInBtn, findsOneWidget);

    await tester.tap(signInBtn);
    await tester.pumpAndSettle();

    expect(find.byType(HomeScreen), findsOneWidget);

    ApiClient.instance = null;
  });
}
