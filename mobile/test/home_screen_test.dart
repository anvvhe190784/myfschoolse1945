import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/home_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/account_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/home_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/notification_screen.dart';
import 'mock_api_client.dart';


void main() {
  group('HomeScreen Tests', () {
    testWidgets('HomeScreen renders AppBar with logo, notification badge and search',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: HomeScreen(studentName: 'Vũ Văn An'),
        ),
      );

      // Verify FPT logo
      expect(find.byType(Image), findsWidgets);
      final imageFinder = find.byWidgetPredicate(
        (widget) =>
            widget is Image &&
            widget.image is AssetImage &&
            (widget.image as AssetImage).assetName == 'assets/images/fpt_logo.png',
      );
      expect(imageFinder, findsOneWidget);

      // Verify notification bell and search icons
      expect(find.byIcon(Icons.notifications_none_rounded), findsOneWidget);
      expect(find.byIcon(Icons.search_rounded), findsOneWidget);
    });

    testWidgets(
        'HomeScreen renders Greeting Banner with dynamic studentName and NO mini calendar',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: HomeScreen(studentName: 'Trần Thị Mai'),
        ),
      );

      // Verify dynamic student greeting
      expect(find.text('Chào buổi sáng, Trần Thị Mai!'), findsOneWidget);
      expect(find.text('Chúc bạn một ngày học tập vui vẻ!'), findsOneWidget);

      // Verify mini calendar icon is NOT present in the banner
      expect(find.byKey(const Key('mini_calendar_badge')), findsNothing);
      expect(find.byIcon(Icons.calendar_month), findsNothing);
      expect(find.byIcon(Icons.calendar_month_outlined), findsNothing);
    });

    testWidgets('HomeScreen renders 8 feature menu items with labels',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: HomeScreen(),
        ),
      );

      // 8 features
      expect(find.text('Lịch học'), findsOneWidget);
      expect(find.text('Bảng điểm'), findsOneWidget);
      expect(find.text('Đơn từ'), findsOneWidget);
      expect(find.text('Câu lạc bộ'), findsOneWidget);
      expect(find.text('Tin tức'), findsOneWidget);
      expect(find.text('Khóa học'), findsOneWidget);
      expect(find.text('Lớp học'), findsOneWidget);
      expect(find.text('Khác'), findsOneWidget);
    });

    testWidgets('HomeScreen renders Tin tức & Sự kiện section and horizontal news cards',
        (WidgetTester tester) async {
      final mockClient = MockApiClient();
      final controller = HomeController(apiClient: mockClient);
      await controller.loadNews();

      await tester.pumpWidget(
        MaterialApp(
          home: HomeScreen(controller: controller),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('Tin tức & Sự kiện'), findsOneWidget);
      expect(find.text('Xem tất cả'), findsOneWidget);

      // Verify news titles loaded dynamically
      expect(
        find.text('Lich thi Hoc ky Fall 2026 chinh thuc'),
        findsOneWidget,
      );
      expect(
        find.text('FPT Edu Camp 2026 sap dien ra'),
        findsOneWidget,
      );
      expect(
        find.text('Thong bao hoc bong FPT Talent 2026'),
        findsOneWidget,
      );
    });

    test('HomeController caps news items to at most 3 items', () async {
      final mockClient = MockApiClient();
      mockClient.mockNewsList = List.generate(
        7,
        (i) => {
          'newsId': i + 1,
          'title': 'Tin tức $i',
          'summary': 'Tóm tắt $i',
          'content': 'Nội dung $i',
          'category': 'GENERAL',
          'publishedAt': '2026-10-14T08:00:00Z',
        },
      );
      final controller = HomeController(apiClient: mockClient);
      await controller.loadNews();

      expect(controller.newsItems.length, 3);
    });

    testWidgets('HomeScreen renders BottomNavigationBar and switches tabs',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: HomeScreen(),
        ),
      );

      expect(find.byType(BottomNavigationBar), findsOneWidget);
      expect(find.text('Trang chủ'), findsOneWidget);
      expect(find.text('Tin nhắn'), findsOneWidget);
      expect(find.text('Hoạt động'), findsOneWidget);
      expect(find.text('Tài khoản'), findsOneWidget);
      expect(find.byIcon(Icons.local_activity_outlined), findsOneWidget);

      // Tap 'Tin nhắn' tab
      await tester.tap(find.text('Tin nhắn'));
      await tester.pump();

      BottomNavigationBar bottomNavBar =
          tester.widget<BottomNavigationBar>(find.byType(BottomNavigationBar));
      expect(bottomNavBar.currentIndex, 1);

      // Tap 'Hoạt động' tab
      await tester.tap(find.text('Hoạt động'));
      await tester.pump();

      bottomNavBar =
          tester.widget<BottomNavigationBar>(find.byType(BottomNavigationBar));
      expect(bottomNavBar.currentIndex, 2);
      expect(
        find.text('Lịch sử điểm danh và sự kiện ngoại khóa'),
        findsOneWidget,
      );
      expect(find.byIcon(Icons.local_activity_outlined), findsNWidgets(2));
    });

    testWidgets('Tapping Tài khoản tab displays AccountScreen',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: HomeScreen(studentName: 'Vũ Văn An'),
        ),
      );

      // Tap 'Tài khoản' tab
      await tester.tap(find.text('Tài khoản'));
      await tester.pumpAndSettle();

      expect(find.byType(AccountScreen), findsOneWidget);
      expect(find.text('Vũ Văn An'), findsOneWidget);
      expect(find.text('Mã học sinh: HS194500'), findsOneWidget);
    });

    testWidgets('HomeScreen displays dynamic unread notification badge on bell icon',
        (WidgetTester tester) async {
      final mockClient = MockApiClient();
      final controller = HomeController(apiClient: mockClient);
      controller.setUnreadNotificationCount(5);

      await tester.pumpWidget(
        MaterialApp(
          home: HomeScreen(controller: controller),
        ),
      );
      await tester.pump();

      expect(find.text('5'), findsOneWidget);
    });

    testWidgets('Tapping notification bell icon opens NotificationScreen',
        (WidgetTester tester) async {
      final mockClient = MockApiClient();
      final controller = HomeController(apiClient: mockClient);

      await tester.pumpWidget(
        MaterialApp(
          home: HomeScreen(controller: controller),
        ),
      );
      await tester.pump();

      // Find bell icon in top bar and tap it
      final bellFinder = find.byIcon(Icons.notifications_none_rounded);
      expect(bellFinder, findsOneWidget);
      await tester.tap(bellFinder);
      await tester.pumpAndSettle();

      expect(find.byType(NotificationScreen), findsOneWidget);
    });
  });
}
