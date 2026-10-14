/// Course Model mapped to database table `courses` and entity `Course.java`.
class CourseModel {
  final int? courseId;
  final String courseCode;
  final String courseName;
  final int credits;
  final String? description;
  final DateTime? createdAt;

  const CourseModel({
    this.courseId,
    required this.courseCode,
    required this.courseName,
    this.credits = 3,
    this.description,
    this.createdAt,
  });

  factory CourseModel.fromJson(Map<String, dynamic> json) {
    return CourseModel(
      courseId: json['courseId'] ?? json['course_id'],
      courseCode: json['courseCode'] ?? json['course_code'] ?? '',
      courseName: json['courseName'] ?? json['course_name'] ?? '',
      credits: json['credits'] ?? 3,
      description: json['description'],
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : (json['created_at'] != null
              ? DateTime.tryParse(json['created_at'].toString())
              : null),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (courseId != null) 'courseId': courseId,
      'courseCode': courseCode,
      'courseName': courseName,
      'credits': credits,
      if (description != null) 'description': description,
      if (createdAt != null) 'createdAt': createdAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory CourseModel.fromMap(Map<String, dynamic> map) => CourseModel.fromJson(map);

  CourseModel copyWith({
    int? courseId,
    String? courseCode,
    String? courseName,
    int? credits,
    String? description,
    DateTime? createdAt,
  }) {
    return CourseModel(
      courseId: courseId ?? this.courseId,
      courseCode: courseCode ?? this.courseCode,
      courseName: courseName ?? this.courseName,
      credits: credits ?? this.credits,
      description: description ?? this.description,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
