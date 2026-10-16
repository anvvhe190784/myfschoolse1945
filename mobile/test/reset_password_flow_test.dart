import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/verify_email_controller.dart';
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

      // Verify Checklist items
      expect(find.text('Điều kiện mật khẩu:'), findsOneWidget);
      expect(find.text('Độ dài từ 8 - 12 ký tự'), findsOneWidget);
      expect(find.text('Ít nhất 1 chữ cái in hoa (A-Z)'), findsOneWidget);
      expect(find.text('Ít nhất 1 chữ cái in thường (a-z)'), findsOneWidget);
      expect(find.text('Ít nhất 1 chữ số (0-9)'), findsOneWidget);
      expect(find.text('Ít nhất 1 ký tự đặc biệt (!@#\$...)'), findsOneWidget);

      // Test visibility toggles
      final visibilityIcons = find.byIcon(Icons.visibility);
      expect(visibilityIcons, findsNWidgets(2));

      await tester.tap(visibilityIcons.first);
      await tester.pump();
      expect(find.byIcon(Icons.visibility_off), findsOneWidget);

      // Initial state: 5 neutral gray checklist items
      expect(find.byIcon(Icons.radio_button_unchecked), findsNWidgets(5));
      expect(find.byIcon(Icons.check_circle_rounded), findsNothing);
      expect(find.byIcon(Icons.cancel_outlined), findsNothing);

      // Submit button is disabled initially
      var submitBtn = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đặt lại mật khẩu'),
      );
      expect(submitBtn.onPressed, isNull);

      // Enter weak new password -> 3 met (length 11, lowercase, digits), 2 unmet in red (uppercase, special char)
      final textFields = find.byType(TextField);
      await tester.enterText(textFields.at(0), 'password123');
      await tester.pump();
      expect(find.byIcon(Icons.cancel_outlined), findsNWidgets(2));
      submitBtn = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đặt lại mật khẩu'),
      );
      expect(submitBtn.onPressed, isNull);

      // Enter valid new password (Pass@1234)
      await tester.enterText(textFields.at(0), 'Pass@1234');
      await tester.pump();

      // All 5 checklist items satisfied (green checkmarks)
      expect(find.byIcon(Icons.check_circle_rounded), findsNWidgets(5));
      expect(find.byIcon(Icons.cancel_outlined), findsNothing);

      // Enter mismatched confirm password -> warning text appears, button still disabled
      await tester.enterText(textFields.at(1), 'Pass@9999');
      await tester.pump();
      expect(find.text('Mật khẩu xác nhận không trùng khớp'), findsOneWidget);
      submitBtn = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đặt lại mật khẩu'),
      );
      expect(submitBtn.onPressed, isNull);

      // Enter matching confirm password -> warning disappears, button unlocked!
      await tester.enterText(textFields.at(1), 'Pass@1234');
      await tester.pump();
      expect(find.text('Mật khẩu xác nhận không trùng khớp'), findsNothing);
      submitBtn = tester.widget<ElevatedButton>(
        find.widgetWithText(ElevatedButton, 'Đặt lại mật khẩu'),
      );
      expect(submitBtn.onPressed, isNotNull);

      // Tap submit button -> success dialog
      final submitFinder = find.widgetWithText(ElevatedButton, 'Đặt lại mật khẩu');
      await tester.ensureVisible(submitFinder);
      await tester.tap(submitFinder);
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

    test('VerifyEmailController onKeyEvent clears previous box on backspace when current is empty', () {
      final controller = VerifyEmailController(email: 'test@fpt.edu.vn');
      controller.controllers[0].text = '7';
      controller.controllers[1].text = '';

      final result = controller.focusNodes[1].onKeyEvent!(
        controller.focusNodes[1],
        const KeyDownEvent(
          physicalKey: PhysicalKeyboardKey.backspace,
          logicalKey: LogicalKeyboardKey.backspace,
          timeStamp: Duration.zero,
        ),
      );

      expect(result, KeyEventResult.handled);
      expect(controller.controllers[0].text, '');
      controller.dispose();
    });

    testWidgets('VerifyEmailScreen supports rapid backspace deletion across OTP boxes',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: VerifyEmailScreen(email: 'user@fpt.edu.vn'),
        ),
      );

      final textFields = find.byType(TextField);
      await tester.enterText(textFields.at(0), '1');
      await tester.enterText(textFields.at(1), '2');
      await tester.enterText(textFields.at(2), '3');
      await tester.pump();

      expect(tester.widget<TextField>(textFields.at(0)).controller?.text, '1');
      expect(tester.widget<TextField>(textFields.at(1)).controller?.text, '2');
      expect(tester.widget<TextField>(textFields.at(2)).controller?.text, '3');

      // Clear 3rd box via text edit -> navigates focus back to 2nd box
      await tester.enterText(textFields.at(2), '');
      await tester.pump();
      expect(tester.widget<TextField>(textFields.at(2)).controller?.text, '');

      // Clear 2nd box -> navigates focus back to 1st box
      await tester.enterText(textFields.at(1), '');
      await tester.pump();
      expect(tester.widget<TextField>(textFields.at(1)).controller?.text, '');
    });

    testWidgets('Entering a digit when previous box is focused routes digit to next empty box',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: VerifyEmailScreen(email: 'user@fpt.edu.vn'),
        ),
      );

      final textFields = find.byType(TextField);
      // Enter 1 in box 0, 2 in box 1, 6 in box 2, 8 in box 3
      await tester.enterText(textFields.at(0), '1');
      await tester.enterText(textFields.at(1), '2');
      await tester.enterText(textFields.at(2), '6');
      await tester.enterText(textFields.at(3), '8');
      await tester.pump();

      expect(tester.widget<TextField>(textFields.at(0)).controller?.text, '1');
      expect(tester.widget<TextField>(textFields.at(1)).controller?.text, '2');
      expect(tester.widget<TextField>(textFields.at(2)).controller?.text, '6');
      expect(tester.widget<TextField>(textFields.at(3)).controller?.text, '8');

      // Delete 8 in box 3 -> box 3 becomes empty, focus returns to box 2
      await tester.enterText(textFields.at(3), '');
      await tester.pump();
      expect(tester.widget<TextField>(textFields.at(3)).controller?.text, '');

      // User enters '9' while box 2 is focused (TextField 2 receives '69')
      await tester.enterText(textFields.at(2), '69');
      await tester.pump();

      // Box 0 and 1 must remain untouched ('1' and '2')
      expect(tester.widget<TextField>(textFields.at(0)).controller?.text, '1');
      expect(tester.widget<TextField>(textFields.at(1)).controller?.text, '2');
      // Box 2 must remain '6' and box 3 must receive '9'
      expect(tester.widget<TextField>(textFields.at(2)).controller?.text, '6');
      expect(tester.widget<TextField>(textFields.at(3)).controller?.text, '9');
    });

    testWidgets('Deleting 6th digit and typing new digit fills 6th box and moves focus to 6th box',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: VerifyEmailScreen(email: 'user@fpt.edu.vn'),
        ),
      );

      final textFields = find.byType(TextField);
      // Fill all 6 boxes
      for (int i = 0; i < 6; i++) {
        await tester.enterText(textFields.at(i), '${i + 1}');
      }
      await tester.pump();

      // Delete 6th digit (box 5) -> box 5 becomes empty, focus returns to box 4
      await tester.enterText(textFields.at(5), '');
      await tester.pump();
      expect(tester.widget<TextField>(textFields.at(5)).controller?.text, '');
      expect(tester.widget<TextField>(textFields.at(4)).focusNode?.hasFocus, isTrue);

      // User enters '9' while box 4 is focused (TextField 4 receives '59')
      await tester.enterText(textFields.at(4), '59');
      await tester.pump();

      // Box 4 keeps '5', Box 5 gets '9'
      expect(tester.widget<TextField>(textFields.at(4)).controller?.text, '5');
      expect(tester.widget<TextField>(textFields.at(5)).controller?.text, '9');

      // Crucial: Focus MUST move to box 5, NOT remain on box 4
      expect(tester.widget<TextField>(textFields.at(5)).focusNode?.hasFocus, isTrue);
      expect(tester.widget<TextField>(textFields.at(4)).focusNode?.hasFocus, isFalse);
    });
  });
}
