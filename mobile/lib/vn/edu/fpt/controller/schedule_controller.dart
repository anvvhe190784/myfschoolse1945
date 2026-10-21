import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/model/schedule_model.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

/// ViewModel managing schedule data, semester filter, and week/day navigation.
class ScheduleController extends ChangeNotifier {
  final ApiClient apiClient;

  DateTime _selectedDate;
  late DateTime _currentWeekStart;
  late DateTime _currentWeekEnd;

  List<ScheduleModel> _weeklySchedules = [];
  List<String> _semesters = ['FALL2026', 'SUMMER2026', 'SPRING2026'];
  String _selectedSemester = 'FALL2026';

  bool _isLoading = false;
  String? _errorMessage;
  bool _disposed = false;

  ScheduleController({
    ApiClient? apiClient,
    DateTime? initialDate,
  })  : apiClient = apiClient ?? ApiClient.instance,
        _selectedDate = initialDate ?? DateTime.now() {
    _computeWeekRange(_selectedDate);
  }

  @override
  void dispose() {
    _disposed = true;
    super.dispose();
  }

  @override
  void notifyListeners() {
    if (!_disposed) {
      super.notifyListeners();
    }
  }

  DateTime get selectedDate => _selectedDate;
  DateTime get currentWeekStart => _currentWeekStart;
  DateTime get currentWeekEnd => _currentWeekEnd;
  List<ScheduleModel> get weeklySchedules => List.unmodifiable(_weeklySchedules);
  List<String> get semesters => List.unmodifiable(_semesters);
  String get selectedSemester => _selectedSemester;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;

  List<ScheduleModel> get dailySchedules {
    final dateStr = _formatYmd(_selectedDate);
    return _weeklySchedules.where((s) => s.date == dateStr).toList();
  }

  void _computeWeekRange(DateTime anchor) {
    // DateTime.monday is 1, sunday is 7
    final weekday = anchor.weekday;
    _currentWeekStart = DateTime(anchor.year, anchor.month, anchor.day - (weekday - 1));
    _currentWeekEnd = DateTime(_currentWeekStart.year, _currentWeekStart.month, _currentWeekStart.day + 6);
  }

  static String _formatYmd(DateTime dt) {
    final y = dt.year.toString().padLeft(4, '0');
    final m = dt.month.toString().padLeft(2, '0');
    final d = dt.day.toString().padLeft(2, '0');
    return '$y-$m-$d';
  }

  bool hasScheduleOn(DateTime date) {
    final dateStr = _formatYmd(date);
    return _weeklySchedules.any((s) => s.date == dateStr);
  }

  Future<void> loadInitialData() async {
    await fetchSemesters();
    await fetchWeeklySchedules();
  }

  Future<void> fetchSemesters() async {
    try {
      final fetched = await apiClient.getSemesters();
      if (fetched.isNotEmpty) {
        _semesters = fetched.map((e) => e.toUpperCase()).toList();
        if (!_semesters.contains(_selectedSemester)) {
          _selectedSemester = _semesters.first;
        }
        notifyListeners();
      }
    } catch (_) {
      // Keep default fallback semesters on error
    }
  }

  Future<void> fetchWeeklySchedules() async {
    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final startStr = _formatYmd(_currentWeekStart);
      final endStr = _formatYmd(_currentWeekEnd);
      final list = await apiClient.getSchedules(startDate: startStr, endDate: endStr);

      _weeklySchedules = list
          .map((item) => ScheduleModel.fromJson(item as Map<String, dynamic>))
          .toList();
      _errorMessage = null;
    } catch (e) {
      _errorMessage = e is ApiException ? e.message : 'Lỗi kết nối khi tải lịch học: $e';
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  void selectDate(DateTime date) {
    _selectedDate = date;
    // Check if new date is outside current week range
    if (date.isBefore(_currentWeekStart) || date.isAfter(_currentWeekEnd)) {
      _computeWeekRange(date);
      fetchWeeklySchedules();
    } else {
      notifyListeners();
    }
  }

  void nextWeek() {
    _computeWeekRange(_currentWeekStart.add(const Duration(days: 7)));
    _selectedDate = _currentWeekStart;
    fetchWeeklySchedules();
  }

  void previousWeek() {
    _computeWeekRange(_currentWeekStart.subtract(const Duration(days: 7)));
    _selectedDate = _currentWeekStart;
    fetchWeeklySchedules();
  }

  void changeSemester(String semester) {
    if (_selectedSemester != semester) {
      _selectedSemester = semester;
      notifyListeners();
      // Optional: align week or refetch if semesters define custom date boundaries
    }
  }

  void setWeeklySchedulesForTesting(List<ScheduleModel> schedules) {
    _weeklySchedules = schedules;
    notifyListeners();
  }
}
