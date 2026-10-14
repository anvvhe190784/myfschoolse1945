/// Grade Model mapped to database table `grades` and backend `GradeDtos.java`.
class GradeModel {
  final int? gradeId;
  final int classId;
  final String? classCode;
  final String? courseName;
  final int? studentId;
  final String assessmentName;
  final double weight;
  final double? score;
  final String? feedback;
  final String status; // 'DRAFT', 'PUBLISHED'
  final DateTime? updatedAt;

  const GradeModel({
    this.gradeId,
    required this.classId,
    this.classCode,
    this.courseName,
    this.studentId,
    required this.assessmentName,
    required this.weight,
    this.score,
    this.feedback,
    this.status = 'PUBLISHED',
    this.updatedAt,
  });

  factory GradeModel.fromJson(Map<String, dynamic> json) {
    return GradeModel(
      gradeId: json['gradeId'] ?? json['grade_id'],
      classId: json['classId'] ?? json['class_id'] ?? 0,
      classCode: json['classCode'] ?? json['class_code'],
      courseName: json['courseName'] ?? json['course_name'],
      studentId: json['studentId'] ?? json['student_id'],
      assessmentName: json['assessmentName'] ??
          json['assessment_name'] ??
          '',
      weight: (json['weight'] as num?)?.toDouble() ?? 0.0,
      score: (json['score'] as num?)?.toDouble(),
      feedback: json['feedback'],
      status: json['status'] ?? 'PUBLISHED',
      updatedAt: json['updatedAt'] != null
          ? DateTime.tryParse(json['updatedAt'].toString())
          : (json['updated_at'] != null
              ? DateTime.tryParse(json['updated_at'].toString())
              : null),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (gradeId != null) 'gradeId': gradeId,
      'classId': classId,
      if (classCode != null) 'classCode': classCode,
      if (courseName != null) 'courseName': courseName,
      if (studentId != null) 'studentId': studentId,
      'assessmentName': assessmentName,
      'weight': weight,
      if (score != null) 'score': score,
      if (feedback != null) 'feedback': feedback,
      'status': status,
      if (updatedAt != null) 'updatedAt': updatedAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory GradeModel.fromMap(Map<String, dynamic> map) => GradeModel.fromJson(map);

  GradeModel copyWith({
    int? gradeId,
    int? classId,
    String? classCode,
    String? courseName,
    int? studentId,
    String? assessmentName,
    double? weight,
    double? score,
    String? feedback,
    String? status,
    DateTime? updatedAt,
  }) {
    return GradeModel(
      gradeId: gradeId ?? this.gradeId,
      classId: classId ?? this.classId,
      classCode: classCode ?? this.classCode,
      courseName: courseName ?? this.courseName,
      studentId: studentId ?? this.studentId,
      assessmentName: assessmentName ?? this.assessmentName,
      weight: weight ?? this.weight,
      score: score ?? this.score,
      feedback: feedback ?? this.feedback,
      status: status ?? this.status,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
