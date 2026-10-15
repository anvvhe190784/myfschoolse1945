/// Class Enrollment Model mapped to database table `class_enrollments`.
class ClassEnrollmentModel {
  final int? enrollmentId;
  final int classId;
  final int studentId;
  final String status; // 'ENROLLED', 'DROPPED', 'COMPLETED'
  final DateTime? enrolledAt;

  const ClassEnrollmentModel({
    this.enrollmentId,
    required this.classId,
    required this.studentId,
    this.status = 'ENROLLED',
    this.enrolledAt,
  });

  factory ClassEnrollmentModel.fromJson(Map<String, dynamic> json) {
    final rawEnrolledAt = json['enrolledAt'] ?? json['enrolled_at'];
    return ClassEnrollmentModel(
      enrollmentId: json['enrollmentId'] ?? json['enrollment_id'],
      classId: json['classId'] ?? json['class_id'] ?? 0,
      studentId: json['studentId'] ?? json['student_id'] ?? 0,
      status: json['status'] ?? 'ENROLLED',
      enrolledAt: rawEnrolledAt != null ? DateTime.tryParse(rawEnrolledAt.toString()) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (enrollmentId != null) 'enrollmentId': enrollmentId,
      'classId': classId,
      'studentId': studentId,
      'status': status,
      if (enrolledAt != null) 'enrolledAt': enrolledAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory ClassEnrollmentModel.fromMap(Map<String, dynamic> map) =>
      ClassEnrollmentModel.fromJson(map);

  ClassEnrollmentModel copyWith({
    int? enrollmentId,
    int? classId,
    int? studentId,
    String? status,
    DateTime? enrolledAt,
  }) {
    return ClassEnrollmentModel(
      enrollmentId: enrollmentId ?? this.enrollmentId,
      classId: classId ?? this.classId,
      studentId: studentId ?? this.studentId,
      status: status ?? this.status,
      enrolledAt: enrolledAt ?? this.enrolledAt,
    );
  }
}
