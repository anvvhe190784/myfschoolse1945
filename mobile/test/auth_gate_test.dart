import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/auth_gate.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/home_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/login_screen.dart';
import 'mock_api_client.dart';

void main() {
  group('AuthGate Widget Tests', () {
    testWidgets('renders LoginScreen when no token is saved in secure storage', (tester) async {
      final client = MockApiClient(); // _mockToken is null by default

      await tester.pumpWidget(
        MaterialApp(
          home: AuthGate(apiClient: client),
        ),
      );

      // Verify progress indicator is initially shown while checking
      expect(find.byType(CircularProgressIndicator), findsOneWidget);

      await tester.pumpAndSettle();

      // Verify it transitions to LoginScreen
      expect(find.byType(LoginScreen), findsOneWidget);
      expect(find.byType(HomeScreen), findsNothing);
    });

    testWidgets('automatically restores session and renders HomeScreen when token exists', (tester) async {
      final client = MockApiClient();
      client.setAuthToken('mock-persisted-jwt-token');

      await tester.pumpWidget(
        MaterialApp(
          home: AuthGate(apiClient: client),
        ),
      );

      await tester.pumpAndSettle();

      // Verify it automatically navigates to HomeScreen without requiring manual login
      expect(find.byType(HomeScreen), findsOneWidget);
      expect(find.byType(LoginScreen), findsNothing);
    });
  });
}
