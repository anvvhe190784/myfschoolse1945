/// Parent-Student Relationship Model mapped to database table `parent_students`.
class ParentStudentModel {
  final int? id;
  final int parentId;
  final int studentId;
  final String relationshipType; // 'FATHER', 'MOTHER', 'GUARDIAN', 'PARENT'
  final DateTime? createdAt;

  const ParentStudentModel({
    this.id,
    required this.parentId,
    required this.studentId,
    this.relationshipType = 'PARENT',
    this.createdAt,
  });

  factory ParentStudentModel.fromJson(Map<String, dynamic> json) {
    final rawCreatedAt = json['createdAt'] ?? json['created_at'];
    return ParentStudentModel(
      id: json['id'],
      parentId: json['parentId'] ?? json['parent_id'] ?? 0,
      studentId: json['studentId'] ?? json['student_id'] ?? 0,
      relationshipType: json['relationshipType'] ??
          json['relationship_type'] ??
          'PARENT',
      createdAt: rawCreatedAt != null ? DateTime.tryParse(rawCreatedAt.toString()) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (id != null) 'id': id,
      'parentId': parentId,
      'studentId': studentId,
      'relationshipType': relationshipType,
      if (createdAt != null) 'createdAt': createdAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory ParentStudentModel.fromMap(Map<String, dynamic> map) =>
      ParentStudentModel.fromJson(map);

  ParentStudentModel copyWith({
    int? id,
    int? parentId,
    int? studentId,
    String? relationshipType,
    DateTime? createdAt,
  }) {
    return ParentStudentModel(
      id: id ?? this.id,
      parentId: parentId ?? this.parentId,
      studentId: studentId ?? this.studentId,
      relationshipType: relationshipType ?? this.relationshipType,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
