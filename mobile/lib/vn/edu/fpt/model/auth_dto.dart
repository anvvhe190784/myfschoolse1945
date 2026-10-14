// Data Transfer Objects for Authentication corresponding to Backend `AuthDtos.java`.

class LoginRequest {
  final String identifier;
  final String password;

  const LoginRequest({
    required this.identifier,
    required this.password,
  });

  Map<String, dynamic> toJson() => {
        'identifier': identifier,
        'password': password,
      };

  factory LoginRequest.fromJson(Map<String, dynamic> json) => LoginRequest(
        identifier: json['identifier'] ?? '',
        password: json['password'] ?? '',
      );
}

class LoginResponse {
  final String accessToken;
  final String refreshToken;
  final String tokenType;
  final int? expiresIn;
  final int userId;
  final String? rollNumber;
  final String email;
  final String fullName;
  final String role;

  // Backwards compatibility getter for `.token`
  String get token => accessToken;

  LoginResponse({
    String? accessToken,
    String? token,
    this.refreshToken = '',
    this.tokenType = 'Bearer',
    this.expiresIn,
    required this.userId,
    this.rollNumber,
    required this.email,
    required this.fullName,
    required this.role,
  }) : accessToken = accessToken ?? token ?? '';

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    final access = json['accessToken'] ?? json['access_token'] ?? json['token'] ?? '';
    final refresh = json['refreshToken'] ?? json['refresh_token'] ?? '';
    return LoginResponse(
      accessToken: access,
      refreshToken: refresh,
      tokenType: json['tokenType'] ?? json['token_type'] ?? 'Bearer',
      expiresIn: json['expiresIn'] ?? json['expires_in'],
      userId: json['userId'] ?? json['user_id'] ?? 0,
      rollNumber: json['rollNumber'] ?? json['roll_number'],
      email: json['email'] ?? '',
      fullName: json['fullName'] ?? json['full_name'] ?? '',
      role: json['role'] ?? 'STUDENT',
    );
  }

  Map<String, dynamic> toJson() => {
        'accessToken': accessToken,
        'refreshToken': refreshToken,
        'token': accessToken,
        'tokenType': tokenType,
        if (expiresIn != null) 'expiresIn': expiresIn,
        'userId': userId,
        if (rollNumber != null) 'rollNumber': rollNumber,
        'email': email,
        'fullName': fullName,
        'role': role,
      };
}

class RefreshTokenRequest {
  final String refreshToken;

  const RefreshTokenRequest({required this.refreshToken});

  Map<String, dynamic> toJson() => {'refreshToken': refreshToken};

  factory RefreshTokenRequest.fromJson(Map<String, dynamic> json) =>
      RefreshTokenRequest(refreshToken: json['refreshToken'] ?? '');
}

class TokenRefreshResponse {
  final String accessToken;
  final String refreshToken;
  final String tokenType;
  final int? expiresIn;

  const TokenRefreshResponse({
    required this.accessToken,
    required this.refreshToken,
    this.tokenType = 'Bearer',
    this.expiresIn,
  });

  factory TokenRefreshResponse.fromJson(Map<String, dynamic> json) =>
      TokenRefreshResponse(
        accessToken: json['accessToken'] ?? json['access_token'] ?? json['token'] ?? '',
        refreshToken: json['refreshToken'] ?? json['refresh_token'] ?? '',
        tokenType: json['tokenType'] ?? json['token_type'] ?? 'Bearer',
        expiresIn: json['expiresIn'] ?? json['expires_in'],
      );

  Map<String, dynamic> toJson() => {
        'accessToken': accessToken,
        'refreshToken': refreshToken,
        'tokenType': tokenType,
        if (expiresIn != null) 'expiresIn': expiresIn,
      };
}

class ResetPasswordRequest {
  final String email;
  final String otp;
  final String newPassword;

  const ResetPasswordRequest({
    required this.email,
    required this.otp,
    required this.newPassword,
  });

  Map<String, dynamic> toJson() => {
        'email': email,
        'otp': otp,
        'newPassword': newPassword,
      };

  factory ResetPasswordRequest.fromJson(Map<String, dynamic> json) =>
      ResetPasswordRequest(
        email: json['email'] ?? '',
        otp: json['otp'] ?? '',
        newPassword: json['newPassword'] ?? '',
      );
}

class UserMeResponse {
  final int userId;
  final String? rollNumber;
  final String email;
  final String fullName;
  final String? phoneNumber;
  final String? avatarUrl;
  final String role;
  final String status;

  const UserMeResponse({
    required this.userId,
    this.rollNumber,
    required this.email,
    required this.fullName,
    this.phoneNumber,
    this.avatarUrl,
    required this.role,
    required this.status,
  });

  factory UserMeResponse.fromJson(Map<String, dynamic> json) => UserMeResponse(
        userId: json['userId'] ?? json['user_id'] ?? 0,
        rollNumber: json['rollNumber'] ?? json['roll_number'],
        email: json['email'] ?? '',
        fullName: json['fullName'] ?? json['full_name'] ?? '',
        phoneNumber: json['phoneNumber'] ?? json['phone_number'],
        avatarUrl: json['avatarUrl'] ?? json['avatar_url'],
        role: json['role'] ?? 'STUDENT',
        status: json['status'] ?? 'ACTIVE',
      );

  Map<String, dynamic> toJson() => {
        'userId': userId,
        if (rollNumber != null) 'rollNumber': rollNumber,
        'email': email,
        'fullName': fullName,
        if (phoneNumber != null) 'phoneNumber': phoneNumber,
        if (avatarUrl != null) 'avatarUrl': avatarUrl,
        'role': role,
        'status': status,
      };
}

class ForgotPasswordRequest {
  final String email;

  const ForgotPasswordRequest({
    required this.email,
  });

  Map<String, dynamic> toJson() => {
        'email': email,
      };

  factory ForgotPasswordRequest.fromJson(Map<String, dynamic> json) =>
      ForgotPasswordRequest(
        email: json['email'] ?? '',
      );
}

class VerifyOtpRequest {
  final String email;
  final String otp;

  const VerifyOtpRequest({
    required this.email,
    required this.otp,
  });

  Map<String, dynamic> toJson() => {
        'email': email,
        'otp': otp,
      };

  factory VerifyOtpRequest.fromJson(Map<String, dynamic> json) =>
      VerifyOtpRequest(
        email: json['email'] ?? '',
        otp: json['otp'] ?? '',
      );
}

class MessageResponse {
  final String message;
  final bool success;

  const MessageResponse({
    required this.message,
    required this.success,
  });

  factory MessageResponse.fromJson(Map<String, dynamic> json) =>
      MessageResponse(
        message: json['message'] ?? '',
        success: json['success'] ?? false,
      );

  Map<String, dynamic> toJson() => {
        'message': message,
        'success': success,
      };
}
