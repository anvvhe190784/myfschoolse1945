/// Schedule Model mapped to database table `schedules` and backend `ScheduleDtos.java`.
class ScheduleModel {
  final int? scheduleId;
  final int classId;
  final String? classCode;
  final String? courseCode;
  final String? courseName;
  final int slotNumber;
  final int sessionNo;
  final String date;
  final String startTime;
  final String endTime;
  final String room;
  final int? lecturerId;
  final String? lecturerName;
  final String? note;
  final String status; // 'UPCOMING', 'PRESENT', 'ABSENT', 'CANCELLED'
  final String? semester;
  final DateTime? createdAt;

  const ScheduleModel({
    this.scheduleId,
    required this.classId,
    this.classCode,
    this.courseCode,
    this.courseName,
    required this.slotNumber,
    this.sessionNo = 1,
    required this.date,
    required this.startTime,
    required this.endTime,
    required this.room,
    this.lecturerId,
    this.lecturerName,
    this.note,
    this.status = 'UPCOMING',
    this.semester,
    this.createdAt,
  });

  factory ScheduleModel.fromJson(Map<String, dynamic> json) {
    final rawCreatedAt = json['createdAt'] ?? json['created_at'];
    return ScheduleModel(
      scheduleId: json['scheduleId'] ?? json['schedule_id'],
      classId: json['classId'] ?? json['class_id'] ?? 0,
      classCode: json['classCode'] ?? json['class_code'],
      courseCode: json['courseCode'] ?? json['course_code'],
      courseName: json['courseName'] ?? json['course_name'],
      slotNumber: json['slotNumber'] ?? json['slot_number'] ?? 1,
      sessionNo: json['sessionNo'] ?? json['session_no'] ?? 1,
      date: json['date']?.toString() ?? '',
      startTime: json['startTime']?.toString() ??
          json['start_time']?.toString() ??
          '',
      endTime: json['endTime']?.toString() ??
          json['end_time']?.toString() ??
          '',
      room: json['room'] ?? '',
      lecturerId: json['lecturerId'] ?? json['lecturer_id'],
      lecturerName: json['lecturerName'] ?? json['lecturer_name'],
      note: json['note'],
      status: json['status'] ?? 'UPCOMING',
      semester: json['semester'],
      createdAt: rawCreatedAt != null ? DateTime.tryParse(rawCreatedAt.toString()) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (scheduleId != null) 'scheduleId': scheduleId,
      'classId': classId,
      if (classCode != null) 'classCode': classCode,
      if (courseCode != null) 'courseCode': courseCode,
      if (courseName != null) 'courseName': courseName,
      'slotNumber': slotNumber,
      'sessionNo': sessionNo,
      'date': date,
      'startTime': startTime,
      'endTime': endTime,
      'room': room,
      if (lecturerId != null) 'lecturerId': lecturerId,
      if (lecturerName != null) 'lecturerName': lecturerName,
      if (note != null) 'note': note,
      'status': status,
      if (semester != null) 'semester': semester,
      if (createdAt != null) 'createdAt': createdAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory ScheduleModel.fromMap(Map<String, dynamic> map) =>
      ScheduleModel.fromJson(map);

  ScheduleModel copyWith({
    int? scheduleId,
    int? classId,
    String? classCode,
    String? courseCode,
    String? courseName,
    int? slotNumber,
    int? sessionNo,
    String? date,
    String? startTime,
    String? endTime,
    String? room,
    int? lecturerId,
    String? lecturerName,
    String? note,
    String? status,
    String? semester,
    DateTime? createdAt,
  }) {
    return ScheduleModel(
      scheduleId: scheduleId ?? this.scheduleId,
      classId: classId ?? this.classId,
      classCode: classCode ?? this.classCode,
      courseCode: courseCode ?? this.courseCode,
      courseName: courseName ?? this.courseName,
      slotNumber: slotNumber ?? this.slotNumber,
      sessionNo: sessionNo ?? this.sessionNo,
      date: date ?? this.date,
      startTime: startTime ?? this.startTime,
      endTime: endTime ?? this.endTime,
      room: room ?? this.room,
      lecturerId: lecturerId ?? this.lecturerId,
      lecturerName: lecturerName ?? this.lecturerName,
      note: note ?? this.note,
      status: status ?? this.status,
      semester: semester ?? this.semester,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
