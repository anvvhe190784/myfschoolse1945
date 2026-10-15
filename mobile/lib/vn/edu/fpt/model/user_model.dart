/// User Model mapped to database table `users` and backend entity `User.java`.
class UserModel {
  final int? userId;
  final String? rollNumber;
  final String email;
  final String? passwordHash;
  final String fullName;
  final String? phoneNumber;
  final String? avatarUrl;
  final String role; // 'STUDENT', 'LECTURER', 'PARENT', 'ADMIN'
  final String status; // 'ACTIVE', 'INACTIVE', 'LOCKED'
  final String? resetToken;
  final DateTime? resetTokenExpiresAt;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  const UserModel({
    this.userId,
    this.rollNumber,
    required this.email,
    this.passwordHash,
    required this.fullName,
    this.phoneNumber,
    this.avatarUrl,
    this.role = 'STUDENT',
    this.status = 'ACTIVE',
    this.resetToken,
    this.resetTokenExpiresAt,
    this.createdAt,
    this.updatedAt,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    final rawResetTokenExpiresAt = json['resetTokenExpiresAt'] ?? json['reset_token_expires_at'];
    final rawCreatedAt = json['createdAt'] ?? json['created_at'];
    final rawUpdatedAt = json['updatedAt'] ?? json['updated_at'];
    return UserModel(
      userId: json['userId'] ?? json['user_id'],
      rollNumber: json['rollNumber'] ?? json['roll_number'],
      email: json['email'] ?? '',
      passwordHash: json['passwordHash'] ?? json['password_hash'],
      fullName: json['fullName'] ?? json['full_name'] ?? '',
      phoneNumber: json['phoneNumber'] ?? json['phone_number'],
      avatarUrl: json['avatarUrl'] ?? json['avatar_url'],
      role: json['role'] ?? 'STUDENT',
      status: json['status'] ?? 'ACTIVE',
      resetToken: json['resetToken'] ?? json['reset_token'],
      resetTokenExpiresAt: rawResetTokenExpiresAt != null
          ? DateTime.tryParse(rawResetTokenExpiresAt.toString())
          : null,
      createdAt: rawCreatedAt != null ? DateTime.tryParse(rawCreatedAt.toString()) : null,
      updatedAt: rawUpdatedAt != null ? DateTime.tryParse(rawUpdatedAt.toString()) : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      if (userId != null) 'userId': userId,
      if (rollNumber != null) 'rollNumber': rollNumber,
      'email': email,
      if (passwordHash != null) 'passwordHash': passwordHash,
      'fullName': fullName,
      if (phoneNumber != null) 'phoneNumber': phoneNumber,
      if (avatarUrl != null) 'avatarUrl': avatarUrl,
      'role': role,
      'status': status,
      if (resetToken != null) 'resetToken': resetToken,
      if (resetTokenExpiresAt != null)
        'resetTokenExpiresAt': resetTokenExpiresAt!.toIso8601String(),
      if (createdAt != null) 'createdAt': createdAt!.toIso8601String(),
      if (updatedAt != null) 'updatedAt': updatedAt!.toIso8601String(),
    };
  }

  Map<String, dynamic> toMap() => toJson();
  factory UserModel.fromMap(Map<String, dynamic> map) => UserModel.fromJson(map);

  UserModel copyWith({
    int? userId,
    String? rollNumber,
    String? email,
    String? passwordHash,
    String? fullName,
    String? phoneNumber,
    String? avatarUrl,
    String role = 'STUDENT',
    String status = 'ACTIVE',
    String? resetToken,
    DateTime? resetTokenExpiresAt,
    DateTime? createdAt,
    DateTime? updatedAt,
  }) {
    return UserModel(
      userId: userId ?? this.userId,
      rollNumber: rollNumber ?? this.rollNumber,
      email: email ?? this.email,
      passwordHash: passwordHash ?? this.passwordHash,
      fullName: fullName ?? this.fullName,
      phoneNumber: phoneNumber ?? this.phoneNumber,
      avatarUrl: avatarUrl ?? this.avatarUrl,
      role: role.isNotEmpty ? role : this.role,
      status: status.isNotEmpty ? status : this.status,
      resetToken: resetToken ?? this.resetToken,
      resetTokenExpiresAt: resetTokenExpiresAt ?? this.resetTokenExpiresAt,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
