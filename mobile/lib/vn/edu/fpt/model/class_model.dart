/// Class Model mapped to database table `classes` and entity `ClassEntity.java`.
class ClassModel {
  final int? classId;
  final String classCode;
  final int courseId;
  final int? lecturerId;
  final String semester;
  final int academicYear;
  final String? roomDefault;
  final DateTime? createdAt;

  const ClassModel({
    this.classId,
    required this.classCode,
    required this.courseId,
    this.lecturerId,
    required this.semester,
    this.academicYear = 2026,
    this.roomDefault,
    this.createdAt,
  });

  factory ClassModel.fromJson(Map<String, dynamic> json) {
    return ClassModel(
      classId: json['classId'] ?? json['class_id'],
      classCode: json['classCode'] ?? json['class_code'] ?? '',
      courseId: json['courseId'] ?? json['course_id'] ?? 0,
      lecturerId: json['lecturerId'] ?? json['lecturer_id'],
      semester: json['semester'] ?? 'Fall2026',
      academicYear: json['academicYear'] ?? json['academic_year'] ?? 2026,
      roomDefault: json['roomDefault'] ?? json['room_default'],
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : (json['created_at'] != null
              ? DateTime.tryParse(json['created_at'].toString())
              : null),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (classId != null) 'classId': classId,
      'classCode': classCode,
      'courseId': courseId,
      if (lecturerId != null) 'lecturerId': lecturerId,
      'semester': semester,
      'academicYear': academicYear,
      if (roomDefault != null) 'roomDefault': roomDefault,
      if (createdAt != null) 'createdAt': createdAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory ClassModel.fromMap(Map<String, dynamic> map) => ClassModel.fromJson(map);

  ClassModel copyWith({
    int? classId,
    String? classCode,
    int? courseId,
    int? lecturerId,
    String? semester,
    int? academicYear,
    String? roomDefault,
    DateTime? createdAt,
  }) {
    return ClassModel(
      classId: classId ?? this.classId,
      classCode: classCode ?? this.classCode,
      courseId: courseId ?? this.courseId,
      lecturerId: lecturerId ?? this.lecturerId,
      semester: semester ?? this.semester,
      academicYear: academicYear ?? this.academicYear,
      roomDefault: roomDefault ?? this.roomDefault,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
