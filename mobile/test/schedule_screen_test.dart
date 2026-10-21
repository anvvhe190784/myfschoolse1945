import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/schedule_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/schedule_screen.dart';
import 'mock_api_client.dart';

void main() {
  group('ScheduleScreen Widget Tests', () {
    late MockApiClient mockApiClient;
    late ScheduleController controller;

    setUp(() {
      mockApiClient = MockApiClient();
      controller = ScheduleController(
        apiClient: mockApiClient,
        initialDate: DateTime(2026, 10, 21),
      );
    });

    tearDown(() {
      controller.dispose();
    });

    Widget createTestWidget() {
      return MaterialApp(
        home: ScheduleScreen(controller: controller),
      );
    }

    testWidgets('Renders AppBar, semester tabs, week navigation, and day picker', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      // 1. AppBar
      expect(find.text('Lịch học'), findsOneWidget);
      expect(find.text('Quay lại'), findsOneWidget);

      // 2. Semester tabs
      expect(find.text('FALL2026'), findsOneWidget);
      expect(find.text('SUMMER2026'), findsOneWidget);
      expect(find.text('SPRING2026'), findsOneWidget);

      // 3. Week navigation header
      expect(find.text('Tháng 10, 2026'), findsOneWidget);
      expect(find.text('Tuần: 19/10 - 25/10'), findsOneWidget);

      // 4. Weekday picker
      expect(find.text('Mon'), findsNWidgets(2)); // Mon in weekday picker + Mon in 1st card
      expect(find.text('Tue'), findsNWidgets(2)); // Tue in weekday picker + Tue in 2nd card
      expect(find.text('Wed'), findsNWidgets(2)); // Wed in weekday picker + Wed in 3rd card
      expect(find.text('Thu'), findsOneWidget);
      expect(find.text('Fri'), findsOneWidget);
      expect(find.text('Sat'), findsOneWidget);
      expect(find.text('Sun'), findsOneWidget);
    });

    testWidgets('Renders schedule cards with slot, room, course, session, and status chips', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      // Section title
      expect(find.text('Danh sách ca học trong tuần'), findsOneWidget);
      expect(find.text('3 ca học'), findsOneWidget);

      // Course code and session no
      expect(find.text('MLN122'), findsNWidgets(2));
      expect(find.text(' • Buổi 4'), findsOneWidget);
      expect(find.text(' • Buổi 5'), findsOneWidget);
      expect(find.text('JIT401'), findsOneWidget);
      expect(find.text(' • Buổi 13'), findsOneWidget);

      // Slots and Rooms
      expect(find.text('Slot 1'), findsNWidgets(2));
      expect(find.text('Slot 2'), findsOneWidget);
      expect(find.text('AL-R402'), findsNWidgets(2));
      expect(find.text('BE-211'), findsOneWidget);

      // Status chips
      expect(find.text('PRESENT'), findsNWidgets(2));
      expect(find.text('ABSENT'), findsOneWidget);

      // Verify that NO Meet URL button is present
      expect(find.text('Meet URL'), findsNothing);
      expect(find.text('🎥 Meet URL'), findsNothing);
    });

    testWidgets('Tapping weekday in picker selects day', (tester) async {
      await tester.pumpWidget(createTestWidget());
      await tester.pumpAndSettle();

      // Tap on day '20' (Tue)
      final day20Finder = find.text('20');
      expect(day20Finder, findsOneWidget);
      await tester.tap(day20Finder);
      await tester.pumpAndSettle();

      expect(controller.selectedDate.day, 20);
    });
  });
}
