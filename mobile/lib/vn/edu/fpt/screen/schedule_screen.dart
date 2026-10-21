import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/schedule_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/model/schedule_model.dart';
import 'package:myfschoolse1945/vn/edu/fpt/widget/bounce_tap.dart';

/// Screen displaying the Student's weekly Timetable / Class Schedule.
/// 100% database-driven from PostgreSQL via Spring Boot REST API.
class ScheduleScreen extends StatefulWidget {
  final ScheduleController? controller;

  const ScheduleScreen({super.key, this.controller});

  @override
  State<ScheduleScreen> createState() => _ScheduleScreenState();
}

class _ScheduleScreenState extends State<ScheduleScreen> {
  late final ScheduleController _controller;
  late final bool _ownsController;

  static const Color primaryOrange = Color(0xFFF97316);
  static const Color primaryDarkOrange = Color(0xFFEA580C);
  static const Color textDark = Color(0xFF0F172A);
  static const Color textMuted = Color(0xFF64748B);
  static const Color bgLight = Color(0xFFF8FAFC);

  @override
  void initState() {
    super.initState();
    _ownsController = widget.controller == null;
    _controller = widget.controller ?? ScheduleController();
    _controller.loadInitialData();
  }

  @override
  void dispose() {
    if (_ownsController) {
      _controller.dispose();
    }
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: bgLight,
      body: SafeArea(
        child: ListenableBuilder(
          listenable: _controller,
          builder: (context, _) {
            return Column(
              children: [
                _buildAppBar(context),
                Expanded(
                  child: RefreshIndicator(
                    color: primaryOrange,
                    onRefresh: () => _controller.fetchWeeklySchedules(),
                    child: SingleChildScrollView(
                      physics: const AlwaysScrollableScrollPhysics(),
                      padding: const EdgeInsets.only(bottom: 24),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          _buildHeaderCard(),
                          const SizedBox(height: 18),
                          _buildSectionTitle(),
                          const SizedBox(height: 12),
                          _buildScheduleContent(),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }

  Widget _buildAppBar(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 10.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          BounceTap(
            onTap: () => Navigator.of(context).maybePop(),
            child: const Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  Icons.arrow_back_ios_new_rounded,
                  size: 18,
                  color: textDark,
                ),
                SizedBox(width: 4),
                Text(
                  'Quay lại',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: textDark,
                  ),
                ),
              ],
            ),
          ),
          const Text(
            'Lịch học',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: textDark,
            ),
          ),
          IconButton(
            icon: const Icon(
              Icons.sync_rounded,
              color: textMuted,
              size: 24,
            ),
            tooltip: 'Làm mới',
            onPressed: () => _controller.fetchWeeklySchedules(),
          ),
        ],
      ),
    );
  }

  Widget _buildHeaderCard() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16.0),
      padding: const EdgeInsets.all(16.0),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        gradient: const LinearGradient(
          colors: [
            primaryOrange,
            Color(0xFFFB923C),
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        boxShadow: [
          BoxShadow(
            color: primaryOrange.withValues(alpha: 0.35),
            blurRadius: 18,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSemesterTabs(),
          const SizedBox(height: 16),
          _buildWeekNavigationHeader(),
          const SizedBox(height: 14),
          _buildWeekdayPicker(),
        ],
      ),
    );
  }

  Widget _buildSemesterTabs() {
    final semesters = _controller.semesters;
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(
        children: semesters.map((sem) {
          final isSelected = sem.toUpperCase() == _controller.selectedSemester.toUpperCase();
          IconData icon;
          if (sem.toUpperCase().contains('FALL')) {
            icon = Icons.eco_rounded;
          } else if (sem.toUpperCase().contains('SUMMER')) {
            icon = Icons.wb_sunny_rounded;
          } else {
            icon = Icons.spa_rounded;
          }

          return Padding(
            padding: const EdgeInsets.only(right: 8.0),
            child: BounceTap(
              onTap: () => _controller.changeSemester(sem),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 200),
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                decoration: BoxDecoration(
                  color: isSelected ? Colors.white : Colors.white.withValues(alpha: 0.22),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      icon,
                      size: 15,
                      color: isSelected ? primaryDarkOrange : Colors.white,
                    ),
                    const SizedBox(width: 6),
                    Text(
                      sem.toUpperCase(),
                      style: TextStyle(
                        color: isSelected ? primaryDarkOrange : Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                        letterSpacing: 0.3,
                      ),
                    ),
                  ],
                ),
              ),
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildWeekNavigationHeader() {
    final start = _controller.currentWeekStart;
    final end = _controller.currentWeekEnd;
    final monthStr = 'Tháng ${start.month}, ${start.year}';
    final weekRangeStr = 'Tuần: ${start.day.toString().padLeft(2, '0')}/${start.month.toString().padLeft(2, '0')} - ${end.day.toString().padLeft(2, '0')}/${end.month.toString().padLeft(2, '0')}';

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        BounceTap(
          onTap: () => _controller.previousWeek(),
          child: const Padding(
            padding: EdgeInsets.all(4.0),
            child: Icon(
              Icons.chevron_left_rounded,
              color: Colors.white,
              size: 26,
            ),
          ),
        ),
        Text(
          monthStr,
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
            fontSize: 15,
          ),
        ),
        Text(
          weekRangeStr,
          style: TextStyle(
            color: Colors.white.withValues(alpha: 0.92),
            fontWeight: FontWeight.w500,
            fontSize: 13,
          ),
        ),
        BounceTap(
          onTap: () => _controller.nextWeek(),
          child: const Padding(
            padding: EdgeInsets.all(4.0),
            child: Icon(
              Icons.chevron_right_rounded,
              color: Colors.white,
              size: 26,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildWeekdayPicker() {
    final weekStart = _controller.currentWeekStart;
    final days = List.generate(7, (i) => weekStart.add(Duration(days: i)));
    const weekdayLabels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: List.generate(7, (index) {
        final date = days[index];
        final isSelected = date.year == _controller.selectedDate.year &&
            date.month == _controller.selectedDate.month &&
            date.day == _controller.selectedDate.day;
        final hasClasses = _controller.hasScheduleOn(date);

        return BounceTap(
          onTap: () => _controller.selectDate(date),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                weekdayLabels[index],
                style: TextStyle(
                  color: Colors.white.withValues(alpha: 0.85),
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                ),
              ),
              const SizedBox(height: 6),
              Container(
                width: 34,
                height: 34,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: isSelected ? Colors.white : Colors.transparent,
                  border: isSelected
                      ? Border.all(color: Colors.white, width: 2)
                      : null,
                ),
                alignment: Alignment.center,
                child: Text(
                  '${date.day}',
                  style: TextStyle(
                    color: isSelected ? primaryDarkOrange : Colors.white,
                    fontWeight: isSelected ? FontWeight.bold : FontWeight.w600,
                    fontSize: 14,
                  ),
                ),
              ),
              const SizedBox(height: 4),
              // Dot indicator if day has classes
              Container(
                width: 5,
                height: 5,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: hasClasses ? Colors.white : Colors.transparent,
                ),
              ),
            ],
          ),
        );
      }),
    );
  }

  Widget _buildSectionTitle() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 18.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          const Text(
            'Danh sách ca học trong tuần',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: textDark,
            ),
          ),
          if (_controller.weeklySchedules.isNotEmpty)
            Text(
              '${_controller.weeklySchedules.length} ca học',
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w500,
                color: textMuted,
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildScheduleContent() {
    if (_controller.isLoading && _controller.weeklySchedules.isEmpty) {
      return const Padding(
        padding: EdgeInsets.symmetric(vertical: 40.0),
        child: Center(
          child: CircularProgressIndicator(
            color: primaryOrange,
          ),
        ),
      );
    }

    if (_controller.errorMessage != null && _controller.weeklySchedules.isEmpty) {
      return Container(
        margin: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 20.0),
        padding: const EdgeInsets.all(20.0),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: Colors.red.shade100),
        ),
        child: Column(
          children: [
            Icon(Icons.error_outline_rounded, color: Colors.red.shade400, size: 40),
            const SizedBox(height: 10),
            Text(
              _controller.errorMessage!,
              textAlign: TextAlign.center,
              style: const TextStyle(color: textDark, fontSize: 14),
            ),
            const SizedBox(height: 12),
            ElevatedButton.icon(
              onPressed: () => _controller.fetchWeeklySchedules(),
              icon: const Icon(Icons.refresh_rounded, size: 18),
              label: const Text('Thử lại'),
              style: ElevatedButton.styleFrom(
                backgroundColor: primaryOrange,
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
          ],
        ),
      );
    }

    final schedules = _controller.weeklySchedules;
    if (schedules.isEmpty) {
      return Container(
        margin: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 24.0),
        padding: const EdgeInsets.symmetric(vertical: 36.0, horizontal: 20.0),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: const Color(0xFFE2E8F0)),
        ),
        child: Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                Icons.event_busy_rounded,
                size: 48,
                color: Colors.grey.shade400,
              ),
              const SizedBox(height: 12),
              const Text(
                'Không có ca học nào trong tuần này',
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w600,
                  color: textDark,
                ),
              ),
              const SizedBox(height: 6),
              const Text(
                'Vui lòng chọn tuần khác hoặc liên hệ phòng đào tạo.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 13,
                  color: textMuted,
                ),
              ),
            ],
          ),
        ),
      );
    }

    return ListView.separated(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      itemCount: schedules.length,
      separatorBuilder: (context, _) => const SizedBox(height: 12),
      itemBuilder: (context, index) {
        final item = schedules[index];
        return _buildScheduleCard(item);
      },
    );
  }

  Widget _buildScheduleCard(ScheduleModel item) {
    // Parse date into Day/Month and Weekday name
    final parsedDate = DateTime.tryParse(item.date);
    String dayMonthStr = item.date;
    String weekdayStr = '';
    if (parsedDate != null) {
      dayMonthStr = '${parsedDate.day.toString().padLeft(2, '0')}/${parsedDate.month.toString().padLeft(2, '0')}';
      const wNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
      weekdayStr = wNames[parsedDate.weekday - 1];
    }

    // Format start and end time (truncate seconds if present)
    String formatTime(String t) {
      if (t.length >= 5) {
        return t.substring(0, 5);
      }
      return t;
    }

    final startStr = formatTime(item.startTime);
    final endStr = formatTime(item.endTime);

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFF1F5F9)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.03),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 1. Left column: Date & Day
          SizedBox(
            width: 50,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  dayMonthStr,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: textDark,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  weekdayStr,
                  style: const TextStyle(
                    fontSize: 13,
                    color: textMuted,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ),

          // Orange vertical separator
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 10),
            width: 2.5,
            height: 60,
            decoration: BoxDecoration(
              color: primaryOrange,
              borderRadius: BorderRadius.circular(2),
            ),
          ),

          // 2. Middle column: Slot & Time
          SizedBox(
            width: 54,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2.5),
                  decoration: BoxDecoration(
                    color: const Color(0xFFFFEDD5),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    'Slot ${item.slotNumber}',
                    style: const TextStyle(
                      color: Color(0xFFC2410C),
                      fontWeight: FontWeight.bold,
                      fontSize: 11,
                    ),
                  ),
                ),
                const SizedBox(height: 6),
                Text(
                  startStr,
                  style: const TextStyle(
                    fontSize: 11,
                    color: textMuted,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const Text(
                  '|',
                  style: TextStyle(
                    fontSize: 10,
                    color: Color(0xFFCBD5E1),
                    height: 1,
                  ),
                ),
                Text(
                  endStr,
                  style: const TextStyle(
                    fontSize: 11,
                    color: textMuted,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(width: 8),

          // 3. Right column: Course, Session, Class, Lecturer, Room, Status
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Top row: Room badge
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: const Color(0xFFFFF7ED),
                        borderRadius: BorderRadius.circular(8),
                        border: Border.all(color: const Color(0xFFFFEDD5)),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const Text(
                            'Phòng học ',
                            style: TextStyle(
                              fontSize: 11,
                              color: textMuted,
                            ),
                          ),
                          Text(
                            item.room,
                            style: const TextStyle(
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                              color: Color(0xFFC2410C),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),

                // Course Code & Session No
                Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      item.courseCode ?? 'MÔN HỌC',
                      style: const TextStyle(
                        fontSize: 15,
                        fontWeight: FontWeight.bold,
                        color: textDark,
                      ),
                    ),
                    Text(
                      ' • Buổi ${item.sessionNo}',
                      style: const TextStyle(
                        fontSize: 13,
                        color: textMuted,
                      ),
                    ),
                  ],
                ),
                if (item.courseName != null && item.courseName!.isNotEmpty) ...[
                  const SizedBox(height: 2),
                  Text(
                    item.courseName!,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                      fontSize: 12,
                      color: textMuted,
                    ),
                  ),
                ],
                const SizedBox(height: 4),

                // Class Code & Lecturer
                Text(
                  'Lớp: ${item.classCode ?? "N/A"} • GV: ${item.lecturerName ?? "Chưa phân công"}',
                  style: const TextStyle(
                    fontSize: 12,
                    color: Color(0xFF475569),
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 8),

                // Status Chip (Present / Absent / Upcoming)
                _buildStatusChip(item.status),

                if (item.note != null && item.note!.trim().isNotEmpty) ...[
                  const SizedBox(height: 6),
                  Text(
                    'Ghi chú: ${item.note}',
                    style: TextStyle(
                      fontSize: 11,
                      fontStyle: FontStyle.italic,
                      color: Colors.grey.shade600,
                    ),
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStatusChip(String status) {
    Color bg;
    Color fg = Colors.white;
    String label;
    IconData icon;

    switch (status.toUpperCase()) {
      case 'PRESENT':
        bg = const Color(0xFF10B981); // Green
        label = 'PRESENT';
        icon = Icons.check_rounded;
        break;
      case 'ABSENT':
        bg = const Color(0xFFEF4444); // Red
        label = 'ABSENT';
        icon = Icons.close_rounded;
        break;
      case 'UPCOMING':
        bg = const Color(0xFF3B82F6); // Blue
        label = 'UPCOMING';
        icon = Icons.schedule_rounded;
        break;
      case 'CANCELLED':
        bg = const Color(0xFF94A3B8); // Slate
        label = 'CANCELLED';
        icon = Icons.block_rounded;
        break;
      default:
        bg = const Color(0xFF0EA5E9);
        label = status.toUpperCase();
        icon = Icons.info_outline_rounded;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 13, color: fg),
          const SizedBox(width: 4),
          Text(
            label,
            style: TextStyle(
              color: fg,
              fontWeight: FontWeight.bold,
              fontSize: 11,
              letterSpacing: 0.3,
            ),
          ),
        ],
      ),
    );
  }
}
