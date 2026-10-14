import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/notification_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/model/notification_model.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/notification_screen.dart';

import 'mock_api_client.dart';

void main() {
  group('NotificationModel Tests', () {
    test('fromJson parses all fields correctly', () {
      final json = {
        'notificationId': 101,
        'userId': 1,
        'title': 'Tiêu đề thông báo',
        'content': 'Nội dung chi tiết',
        'type': 'NEWS',
        'targetId': 42,
        'isRead': false,
        'createdAt': '2026-10-14T08:00:00Z',
      };

      final model = NotificationModel.fromJson(json);

      expect(model.notificationId, 101);
      expect(model.userId, 1);
      expect(model.title, 'Tiêu đề thông báo');
      expect(model.content, 'Nội dung chi tiết');
      expect(model.type, 'NEWS');
      expect(model.targetId, 42);
      expect(model.isRead, isFalse);
      expect(model.typeLabel, 'Tin tức');
      expect(model.icon, Icons.campaign_rounded);
    });

    test('Helper getters handle different notification types', () {
      final secModel = NotificationModel(
        notificationId: 1,
        title: 'Bảo mật',
        content: 'Mật khẩu đã đổi',
        type: 'SECURITY',
        createdAt: DateTime.now(),
      );
      expect(secModel.typeLabel, 'Bảo mật');
      expect(secModel.icon, Icons.shield_outlined);

      final acadModel = NotificationModel(
        notificationId: 2,
        title: 'Học vụ',
        content: 'Lịch thi',
        type: 'ACADEMIC',
        createdAt: DateTime.now().subtract(const Duration(minutes: 5)),
      );
      expect(acadModel.typeLabel, 'Học vụ');
      expect(acadModel.formattedTime, '5 phút trước');

      final reqModel = NotificationModel(
        notificationId: 3,
        title: 'Đơn từ',
        content: 'Đã duyệt',
        type: 'REQUEST',
        createdAt: DateTime.now().subtract(const Duration(hours: 2)),
      );
      expect(reqModel.typeLabel, 'Đơn từ');
      expect(reqModel.formattedTime, '2 giờ trước');
    });

    test('copyWith updates isRead state cleanly', () {
      final model = NotificationModel(
        notificationId: 1,
        title: 'Test',
        content: 'Content',
        type: 'SYSTEM',
        isRead: false,
        createdAt: DateTime.now(),
      );

      final updated = model.copyWith(isRead: true);
      expect(updated.isRead, isTrue);
      expect(updated.title, 'Test');
    });
  });

  group('NotificationController Unit Tests', () {
    late MockApiClient mockApi;
    late NotificationController controller;

    setUp(() {
      mockApi = MockApiClient();
      controller = NotificationController(apiClient: mockApi);
    });

    tearDown(() {
      controller.dispose();
    });

    test('loadNotifications fetches items and sets unreadCount', () async {
      await controller.loadNotifications();

      expect(controller.notifications.length, 3);
      expect(controller.unreadCount, 2); // 101 and 103 are unread
      expect(controller.isLoading, isFalse);
    });

    test('Filtering toggles between all and unread items', () async {
      await controller.loadNotifications();

      expect(controller.filteredNotifications.length, 3);

      controller.setFilter(NotificationFilter.unread);
      expect(controller.filteredNotifications.length, 2);
      expect(controller.filteredNotifications.every((n) => !n.isRead), isTrue);

      controller.setFilter(NotificationFilter.all);
      expect(controller.filteredNotifications.length, 3);
    });

    test('markAsRead updates item isRead and unreadCount', () async {
      await controller.loadNotifications();
      expect(controller.unreadCount, 2);

      await controller.markAsRead(101);

      expect(controller.unreadCount, 1);
      final item = controller.notifications.firstWhere((n) => n.notificationId == 101);
      expect(item.isRead, isTrue);
    });

    test('markAllAsRead marks all notifications as read', () async {
      await controller.loadNotifications();
      expect(controller.unreadCount, 2);

      await controller.markAllAsRead();

      expect(controller.unreadCount, 0);
      expect(controller.notifications.every((n) => n.isRead), isTrue);
    });
  });

  group('NotificationScreen Widget Tests', () {
    late MockApiClient mockApi;
    late NotificationController controller;

    setUp(() {
      mockApi = MockApiClient();
      controller = NotificationController(apiClient: mockApi);
    });

    tearDown(() {
      controller.dispose();
    });

    testWidgets('Renders AppBar, filter chips, and notification cards', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: NotificationScreen(controller: controller),
        ),
      );

      // Settle async fetch
      await tester.pumpAndSettle();

      expect(find.text('Thông báo'), findsOneWidget);
      expect(find.text('Đọc tất cả'), findsOneWidget);
      expect(find.textContaining('Tất cả'), findsOneWidget);
      expect(find.textContaining('Chưa đọc'), findsOneWidget);

      expect(find.text('Lịch thi Học kỳ Fall 2026 chính thức'), findsOneWidget);
      expect(find.text('Bảo mật: Mật khẩu đã được cập nhật'), findsOneWidget);
      expect(find.text('Nhắc nhở học vụ: Nộp đơn xét tốt nghiệp'), findsOneWidget);
    });

    testWidgets('Tapping filter chip filters unread items', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: NotificationScreen(controller: controller),
        ),
      );
      await tester.pumpAndSettle();

      // Tap on 'Chưa đọc'
      await tester.tap(find.textContaining('Chưa đọc'));
      await tester.pumpAndSettle();

      // Unread items should be visible
      expect(find.text('Lịch thi Học kỳ Fall 2026 chính thức'), findsOneWidget);
      expect(find.text('Nhắc nhở học vụ: Nộp đơn xét tốt nghiệp'), findsOneWidget);
      // Already read item should be filtered out
      expect(find.text('Bảo mật: Mật khẩu đã được cập nhật'), findsNothing);
    });

    testWidgets('Tapping notification opens detail bottom sheet and marks as read', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: NotificationScreen(controller: controller),
        ),
      );
      await tester.pumpAndSettle();

      // Tap on unread item 101
      await tester.tap(find.text('Lịch thi Học kỳ Fall 2026 chính thức'));
      await tester.pumpAndSettle();

      // Bottom sheet should be visible with 'Đóng' button
      expect(find.text('Đóng'), findsOneWidget);
      expect(
        find.descendant(
          of: find.byType(BottomSheet),
          matching: find.text('Phòng Đào tạo đã công bố lịch thi các môn chuyên ngành.'),
        ),
        findsOneWidget,
      );

      // Close bottom sheet
      await tester.tap(find.text('Đóng'));
      await tester.pumpAndSettle();

      // Item 101 is now read
      expect(controller.unreadCount, 1);
    });

    testWidgets('Tapping Đọc tất cả marks everything read', (tester) async {
      await tester.pumpWidget(
        MaterialApp(
          home: NotificationScreen(controller: controller),
        ),
      );
      await tester.pumpAndSettle();

      expect(find.text('Đọc tất cả'), findsOneWidget);
      await tester.tap(find.text('Đọc tất cả'));
      await tester.pumpAndSettle();

      expect(controller.unreadCount, 0);
      expect(find.text('Đọc tất cả'), findsNothing);
    });
  });
}
