/// Request Model mapped to database table `requests` and backend `RequestDtos.java`.
class RequestModel {
  final int? requestId;
  final int studentId;
  final String? studentName;
  final String? studentRollNumber;
  final int? requesterId;
  final String requestType; // 'ATTENDANCE_EXEMPTION', 'COURSE_WITHDRAWAL', etc.
  final String title;
  final String content;
  final String? attachmentUrl;
  final String status; // 'PENDING', 'PROCESSING', 'APPROVED', 'REJECTED'
  final int? handlerId;
  final String? responseNote;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  const RequestModel({
    this.requestId,
    required this.studentId,
    this.studentName,
    this.studentRollNumber,
    this.requesterId,
    required this.requestType,
    required this.title,
    required this.content,
    this.attachmentUrl,
    this.status = 'PENDING',
    this.handlerId,
    this.responseNote,
    this.createdAt,
    this.updatedAt,
  });

  factory RequestModel.fromJson(Map<String, dynamic> json) {
    return RequestModel(
      requestId: json['requestId'] ?? json['request_id'],
      studentId: json['studentId'] ?? json['student_id'] ?? 0,
      studentName: json['studentName'] ?? json['student_name'],
      studentRollNumber:
          json['studentRollNumber'] ?? json['student_roll_number'],
      requesterId: json['requesterId'] ?? json['requester_id'],
      requestType: json['requestType'] ?? json['request_type'] ?? 'OTHER',
      title: json['title'] ?? '',
      content: json['content'] ?? '',
      attachmentUrl: json['attachmentUrl'] ?? json['attachment_url'],
      status: json['status'] ?? 'PENDING',
      handlerId: json['handlerId'] ?? json['handler_id'],
      responseNote: json['responseNote'] ?? json['response_note'],
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : (json['created_at'] != null
              ? DateTime.tryParse(json['created_at'].toString())
              : null),
      updatedAt: json['updatedAt'] != null
          ? DateTime.tryParse(json['updatedAt'].toString())
          : (json['updated_at'] != null
              ? DateTime.tryParse(json['updated_at'].toString())
              : null),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (requestId != null) 'requestId': requestId,
      'studentId': studentId,
      if (studentName != null) 'studentName': studentName,
      if (studentRollNumber != null) 'studentRollNumber': studentRollNumber,
      if (requesterId != null) 'requesterId': requesterId,
      'requestType': requestType,
      'title': title,
      'content': content,
      if (attachmentUrl != null) 'attachmentUrl': attachmentUrl,
      'status': status,
      if (handlerId != null) 'handlerId': handlerId,
      if (responseNote != null) 'responseNote': responseNote,
      if (createdAt != null) 'createdAt': createdAt!.toIso8601String(),
      if (updatedAt != null) 'updatedAt': updatedAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory RequestModel.fromMap(Map<String, dynamic> map) =>
      RequestModel.fromJson(map);

  RequestModel copyWith({
    int? requestId,
    int? studentId,
    String? studentName,
    String? studentRollNumber,
    int? requesterId,
    String? requestType,
    String title = '',
    String content = '',
    String? attachmentUrl,
    String status = 'PENDING',
    int? handlerId,
    String? responseNote,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return RequestModel(
      requestId: requestId ?? this.requestId,
      studentId: studentId ?? this.studentId,
      studentName: studentName ?? this.studentName,
      studentRollNumber: studentRollNumber ?? this.studentRollNumber,
      requesterId: requesterId ?? this.requesterId,
      requestType: requestType ?? this.requestType,
      title: title.isNotEmpty ? title : this.title,
      content: content.isNotEmpty ? content : this.content,
      attachmentUrl: attachmentUrl ?? this.attachmentUrl,
      status: status.isNotEmpty ? status : this.status,
      handlerId: handlerId ?? this.handlerId,
      responseNote: responseNote ?? this.responseNote,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}

/// DTO for creating a new request via `POST /api/v1/requests`
class CreateRequestDto {
  final String requestType;
  final String title;
  final String content;
  final String? attachmentUrl;

  const CreateRequestDto({
    required this.requestType,
    required this.title,
    required this.content,
    this.attachmentUrl,
  });

  Map<String, dynamic> toJson() => {
        'requestType': requestType,
        'title': title,
        'content': content,
        if (attachmentUrl != null) 'attachmentUrl': attachmentUrl,
      };

  factory CreateRequestDto.fromJson(Map<String, dynamic> json) =>
      CreateRequestDto(
        requestType: json['requestType'] ?? '',
        title: json['title'] ?? '',
        content: json['content'] ?? '',
        attachmentUrl: json['attachmentUrl'],
      );
}
