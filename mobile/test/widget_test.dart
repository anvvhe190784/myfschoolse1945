import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/main.dart';

void main() {
  testWidgets('App smoke test renders LoginScreen', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MyApp());

    // Verify that LoginScreen is rendered
    expect(find.text('Đăng nhập'), findsNWidgets(2));
    expect(find.byType(Image), findsOneWidget);
    expect(find.widgetWithText(ElevatedButton, 'Đăng nhập'), findsOneWidget);
  });
}
