import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/forgot_password_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/login_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/reset_password_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/verify_email_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';
import 'mock_api_client.dart';

void main() {
  group('Reset Password Flow Tests', () {
    setUp(() {
      ApiClient.instance = MockApiClient();
    });

    tearDown(() {
      ApiClient.instance = null;
    });

    testWidgets('LoginScreen navigates to ForgotPasswordScreen on click',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: LoginScreen(),
        ),
      );

      final forgotPasswordBtn = find.text('Quên mật khẩu?');
      expect(forgotPasswordBtn, findsOneWidget);

      await tester.tap(forgotPasswordBtn);
      await tester.pumpAndSettle();

      expect(find.byType(ForgotPasswordScreen), findsOneWidget);
      expect(find.text('Quên mật khẩu?'), findsOneWidget);
    });

    testWidgets('ForgotPasswordScreen renders all UI elements and validates email',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ForgotPasswordScreen(),
        ),
      );

      // Verify FPT logo
      expect(find.byType(Image), findsOneWidget);
      final imageWidget = tester.widget<Image>(find.byType(Image));
      expect(
          (imageWidget.image as AssetImage).assetName, 'assets/images/fpt_logo.png');

      // Verify Header & Subtitle
      expect(find.text('Quên mật khẩu?'), findsOneWidget);
      expect(
        find.text(
            "Nhập địa chỉ email của bạn để nhận\nmã xác thực đặt lại mật khẩu."),
        findsOneWidget,
      );

      // Verify Email Field
      expect(find.text('Email'), findsOneWidget);
      expect(find.byType(TextField), findsOneWidget);

      // Verify Send Button & Back link
      expect(find.text('Gửi mã xác nhận'), findsOneWidget);
      expect(find.text('Quay lại đăng nhập'), findsOneWidget);

      // Tap Send without email -> error snackbar
      await tester.tap(find.text('Gửi mã xác nhận'));
      await tester.pump();
      expect(find.text('Vui lòng nhập địa chỉ email của bạn'), findsOneWidget);

      // Enter email and submit -> navigates to VerifyEmailScreen
      await tester.enterText(find.byType(TextField), 'student@fpt.edu.vn');
      await tester.tap(find.text('Gửi mã xác nhận'));
      await tester.pumpAndSettle();

      expect(find.byType(VerifyEmailScreen), findsOneWidget);
      expect(find.text('Xác thực email'), findsWidgets);
      expect(
        find.text('Chúng tôi đã gửi email xác thực đến\nstudent@fpt.edu.vn'),
        findsOneWidget,
      );
    });

    testWidgets('VerifyEmailScreen renders 6 OTP boxes, countdown timer, and navigates',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: VerifyEmailScreen(email: 'user@fpt.edu.vn'),
        ),
      );

      // Verify Header
      expect(find.text('Xác thực email'), findsWidgets);
      expect(
        find.text('Chúng tôi đã gửi email xác thực đến\nuser@fpt.edu.vn'),
        findsOneWidget,
      );

      // Verify Mail badge icon
      expect(find.byIcon(Icons.mail_outline_rounded), findsOneWidget);

      // Verify OTP label & 6 input boxes
      expect(find.text('Nhập mã xác thực 6 chữ số'), findsOneWidget);
      expect(find.byType(TextField), findsNWidgets(6));

      // Verify Countdown timer text
      expect(find.textContaining("Chưa nhận được mã?"), findsOneWidget);
      expect(find.textContaining('Gửi lại (00:'), findsOneWidget);

      // Verify button & link
      expect(find.widgetWithText(ElevatedButton, 'Xác thực email'), findsOneWidget);
      expect(find.text('Quay lại đăng nhập'), findsOneWidget);

      // Tap verify without code -> warning snackbar
      await tester.tap(find.widgetWithText(ElevatedButton, 'Xác thực email'));
      await tester.pump();
      expect(
        find.text('Vui lòng nhập đầy đủ mã xác thực 6 chữ số'),
        findsOneWidget,
      );

      // Fill in all 6 OTP digits
      final textFields = find.byType(TextField);
      await tester.enterText(textFields.at(0), '4');
      await tester.enterText(textFields.at(1), '8');
      await tester.enterText(textFields.at(2), '2');
      await tester.enterText(textFields.at(3), '9');
      await tester.enterText(textFields.at(4), '1');
      await tester.enterText(textFields.at(5), '5');
      await tester.pump();

      // Tap verify button -> navigates to ResetPasswordScreen
      await tester.tap(find.widgetWithText(ElevatedButton, 'Xác thực email'));
      await tester.pumpAndSettle();

      expect(find.byType(ResetPasswordScreen), findsOneWidget);
      expect(find.text('Đặt lại mật khẩu'), findsWidgets);
    });

    testWidgets('ResetPasswordScreen validates input, toggles visibility, and submits',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: ResetPasswordScreen(email: 'user@fpt.edu.vn'),
        ),
      );

      // Verify Header & Subtitle
      expect(find.text('Đặt lại mật khẩu'), findsWidgets);
      expect(
        find.text(
            'Email đã được xác thực thành công. Vui lòng\ntạo mật khẩu mới bên dưới.'),
        findsOneWidget,
      );

      // Verify Fields
      expect(find.text('Mật khẩu mới'), findsOneWidget);
      expect(find.text('Xác nhận mật khẩu'), findsOneWidget);
      expect(find.byType(TextField), findsNWidgets(2));

      // Test visibility toggles
      final visibilityIcons = find.byIcon(Icons.visibility);
      expect(visibilityIcons, findsNWidgets(2));

      await tester.tap(visibilityIcons.first);
      await tester.pump();
      expect(find.byIcon(Icons.visibility_off), findsOneWidget);

      // Tap submit with empty fields -> snackbar
      await tester.tap(find.widgetWithText(ElevatedButton, 'Đặt lại mật khẩu'));
      await tester.pump();
      expect(find.text('Vui lòng điền cả 2 trường mật khẩu'), findsOneWidget);

      // Enter mismatched passwords
      final textFields = find.byType(TextField);
      await tester.enterText(textFields.at(0), 'password123');
      await tester.enterText(textFields.at(1), 'password999');
      await tester.tap(find.widgetWithText(ElevatedButton, 'Đặt lại mật khẩu'));
      await tester.pump();
      expect(find.text('Mật khẩu xác nhận không khớp'), findsOneWidget);

      // Enter matching passwords
      await tester.enterText(textFields.at(1), 'password123');
      await tester.tap(find.widgetWithText(ElevatedButton, 'Đặt lại mật khẩu'));
      await tester.pumpAndSettle();

      // Dialog appears
      expect(find.text('Thành công'), findsOneWidget);
      expect(
        find.text(
            'Đặt lại mật khẩu thành công. Vui lòng đăng nhập bằng mật khẩu mới.'),
        findsOneWidget,
      );
      expect(find.text('Đăng nhập ngay'), findsOneWidget);
    });
  });
}
