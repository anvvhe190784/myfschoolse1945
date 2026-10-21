import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/schedule_controller.dart';
import 'mock_api_client.dart';

void main() {
  group('ScheduleController Unit Tests', () {
    late MockApiClient mockApiClient;
    late ScheduleController controller;

    setUp(() {
      mockApiClient = MockApiClient();
      // Anchor to Wednesday Oct 21, 2026 (matching mockup context)
      controller = ScheduleController(
        apiClient: mockApiClient,
        initialDate: DateTime(2026, 10, 21),
      );
    });

    tearDown(() {
      controller.dispose();
    });

    test('Initializes with correct week boundaries (Monday to Sunday)', () {
      expect(controller.selectedDate, DateTime(2026, 10, 21));
      expect(controller.currentWeekStart, DateTime(2026, 10, 19)); // Monday
      expect(controller.currentWeekEnd, DateTime(2026, 10, 25)); // Sunday
    });

    test('Loads semesters and weekly schedules from ApiClient', () async {
      await controller.loadInitialData();

      expect(controller.semesters, contains('FALL2026'));
      expect(controller.weeklySchedules.length, 3);
      expect(controller.weeklySchedules[0].courseCode, 'MLN122');
      expect(controller.weeklySchedules[0].slotNumber, 2);
      expect(controller.weeklySchedules[0].status, 'PRESENT');
      expect(controller.weeklySchedules[0].room, 'AL-R402');
    });

    test('dailySchedules filters schedules by selectedDate', () async {
      await controller.loadInitialData();

      // Oct 21, 2026 should have 1 schedule (MLN122 session 5)
      expect(controller.dailySchedules.length, 1);
      expect(controller.dailySchedules.first.sessionNo, 5);
      expect(controller.dailySchedules.first.status, 'ABSENT');

      // Select Oct 19, 2026
      controller.selectDate(DateTime(2026, 10, 19));
      expect(controller.dailySchedules.length, 1);
      expect(controller.dailySchedules.first.sessionNo, 4);
      expect(controller.dailySchedules.first.status, 'PRESENT');

      // Select Oct 22, 2026 (no class in mock data)
      controller.selectDate(DateTime(2026, 10, 22));
      expect(controller.dailySchedules.isEmpty, isTrue);
    });

    test('hasScheduleOn correctly identifies days with classes', () async {
      await controller.loadInitialData();

      expect(controller.hasScheduleOn(DateTime(2026, 10, 19)), isTrue);
      expect(controller.hasScheduleOn(DateTime(2026, 10, 20)), isTrue);
      expect(controller.hasScheduleOn(DateTime(2026, 10, 21)), isTrue);
      expect(controller.hasScheduleOn(DateTime(2026, 10, 22)), isFalse);
      expect(controller.hasScheduleOn(DateTime(2026, 10, 23)), isFalse);
    });

    test('nextWeek and previousWeek shift week range by 7 days', () async {
      await controller.loadInitialData();

      controller.nextWeek();
      expect(controller.currentWeekStart, DateTime(2026, 10, 26));
      expect(controller.currentWeekEnd, DateTime(2026, 11, 1));

      controller.previousWeek();
      expect(controller.currentWeekStart, DateTime(2026, 10, 19));
      expect(controller.currentWeekEnd, DateTime(2026, 10, 25));
    });

    test('changeSemester updates selectedSemester', () {
      controller.changeSemester('SUMMER2026');
      expect(controller.selectedSemester, 'SUMMER2026');
    });
  });
}
