import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

/// Mock ApiClient for unit and widget tests to isolate UI from network calls.
class MockApiClient extends ApiClient {
  @override
  Future<Map<String, dynamic>> forgotPassword({required String email}) async {
    return {
      'message': 'Mã xác thực OTP đã được gửi đến email của bạn.',
      'success': true,
    };
  }

  @override
  Future<Map<String, dynamic>> verifyOtp({
    required String email,
    required String otp,
  }) async {
    return {
      'message': 'Xác thực OTP thành công!',
      'success': true,
      'resetToken': 'mock-reset-token-12345',
    };
  }

  @override
  Future<Map<String, dynamic>> resetPassword({
    required String newPassword,
    String? email,
    String? otp,
    String? resetToken,
  }) async {
    return {
      'message': 'Đặt lại mật khẩu thành công!',
      'success': true,
    };
  }

  bool changePasswordShouldSucceed = true;
  String? changePasswordErrorMessage;

  @override
  Future<Map<String, dynamic>> changePassword({
    required String oldPassword,
    required String newPassword,
  }) async {
    if (!changePasswordShouldSucceed) {
      throw ApiException(
        statusCode: 400,
        message: changePasswordErrorMessage ?? 'Mật khẩu hiện tại không chính xác',
      );
    }
    return {
      'message': 'Đổi mật khẩu thành công!',
      'success': true,
    };
  }

  String? _mockToken;
  String? _mockRefreshToken;

  @override
  void setAuthToken(String token, [String? refreshToken]) {
    _mockToken = token;
    if (refreshToken != null) _mockRefreshToken = refreshToken;
  }

  @override
  String? get authToken => _mockToken;

  @override
  String? get refreshToken => _mockRefreshToken;

  @override
  bool get isAuthenticated => _mockToken != null && _mockToken!.isNotEmpty;

  @override
  Future<void> saveTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    _mockToken = accessToken;
    _mockRefreshToken = refreshToken;
  }

  @override
  Future<void> saveAuthToken(String token) async {
    _mockToken = token;
  }

  @override
  Future<String?> restoreSession() async {
    return _mockToken;
  }

  @override
  Future<bool> refreshAccessToken() async {
    if (_mockRefreshToken == null || _mockRefreshToken!.isEmpty) return false;
    _mockToken = 'rotated-access-token';
    _mockRefreshToken = 'rotated-refresh-token';
    return true;
  }

  @override
  Future<void> clearAuthToken() async {
    _mockToken = null;
    _mockRefreshToken = null;
  }

  @override
  Future<void> logout() async {
    _mockToken = null;
    _mockRefreshToken = null;
  }

  @override
  Future<Map<String, dynamic>> getCurrentUser() async {
    return {
      'userId': 1,
      'fullName': 'Mock User',
      'email': 'mock@fpt.edu.vn',
      'rollNumber': 'SE12345',
      'role': 'STUDENT',
      'phoneNumber': '0912345678',
      'phone': '0912345678',
      'studentId': 'HS194500',
    };
  }

  @override
  Future<Map<String, dynamic>> login({
    required String identifier,
    required String password,
  }) async {
    _mockToken = 'mock-jwt-token';
    _mockRefreshToken = 'mock-refresh-token';
    return {
      'accessToken': 'mock-jwt-token',
      'refreshToken': 'mock-refresh-token',
      'token': 'mock-jwt-token',
      'tokenType': 'Bearer',
      'userId': 1,
      'rollNumber': 'SE12345',
      'email': identifier,
      'fullName': 'Mock User',
      'role': 'STUDENT',
    };
  }

  List<Map<String, dynamic>> mockNewsList = [
    {
      'newsId': 1,
      'title': 'Lich thi Hoc ky Fall 2026 chinh thuc',
      'summary': 'Phong Dao tao thong bao lich thi cac mon chuyen nganh SE/PRM cho sinh vien.',
      'content': 'Chi tiet ca thi va phong thi da duoc cap nhat.',
      'category': 'ACADEMIC',
      'publishedAt': '2026-10-14T08:00:00Z',
    },
    {
      'newsId': 2,
      'title': 'FPT Edu Camp 2026 sap dien ra',
      'summary': 'Ngay hoi giao luu cong nghe va hoat dong ngoai khoa quy mo lon.',
      'content': 'Tham gia ngay de trai nghiem.',
      'category': 'EVENT',
      'publishedAt': '2026-10-12T09:00:00Z',
    },
    {
      'newsId': 3,
      'title': 'Thong bao hoc bong FPT Talent 2026',
      'summary': 'Chuong trinh hoc bong danh cho cac sinh vien co thanh tich xuat sac.',
      'content': 'Cac ban sinh vien dat GPA tren 8.5.',
      'category': 'SCHOLARSHIP',
      'publishedAt': '2026-10-10T10:00:00Z',
    },
  ];

  @override
  Future<List<dynamic>> getNews({int? limit}) async {
    if (limit != null && limit > 0) {
      return mockNewsList.take(limit).toList();
    }
    return mockNewsList;
  }

  List<Map<String, dynamic>> mockNotificationList = [
    {
      'notificationId': 101,
      'userId': 1,
      'title': 'Lịch thi Học kỳ Fall 2026 chính thức',
      'content': 'Phòng Đào tạo đã công bố lịch thi các môn chuyên ngành.',
      'type': 'NEWS',
      'targetId': 1,
      'isRead': false,
      'createdAt': '2026-10-14T08:00:00Z',
    },
    {
      'notificationId': 102,
      'userId': 1,
      'title': 'Bảo mật: Mật khẩu đã được cập nhật',
      'content': 'Mật khẩu tài khoản của bạn đã được thay đổi thành công.',
      'type': 'SECURITY',
      'targetId': null,
      'isRead': true,
      'createdAt': '2026-10-13T10:00:00Z',
    },
    {
      'notificationId': 103,
      'userId': 1,
      'title': 'Nhắc nhở học vụ: Nộp đơn xét tốt nghiệp',
      'content': 'Hạn chót nộp đơn xét tốt nghiệp đợt 3 là ngày 15/11.',
      'type': 'ACADEMIC',
      'targetId': null,
      'isRead': false,
      'createdAt': '2026-10-14T06:00:00Z',
    },
  ];

  @override
  Future<dynamic> getNotifications({
    int page = 0,
    int size = 10,
    bool unreadOnly = false,
  }) async {
    var list = mockNotificationList;
    if (unreadOnly) {
      list = list.where((n) => n['isRead'] == false).toList();
    }
    final start = page * size;
    if (start >= list.length) {
      return {
        'content': <Map<String, dynamic>>[],
        'totalPages': (list.length / size).ceil(),
        'totalElements': list.length,
        'last': true,
        'number': page,
        'size': size,
      };
    }
    final end = (start + size < list.length) ? start + size : list.length;
    final paged = list.sublist(start, end);
    return {
      'content': paged,
      'totalPages': (list.length / size).ceil(),
      'totalElements': list.length,
      'last': end >= list.length,
      'number': page,
      'size': size,
    };
  }

  @override
  Future<int> getUnreadNotificationCount() async {
    return mockNotificationList.where((n) => n['isRead'] == false).length;
  }

  @override
  Future<Map<String, dynamic>> markNotificationRead(int id) async {
    final item = mockNotificationList.firstWhere(
      (n) => n['notificationId'] == id,
      orElse: () => {},
    );
    if (item.isNotEmpty) {
      item['isRead'] = true;
    }
    return item;
  }

  @override
  Future<Map<String, dynamic>> markAllNotificationsRead() async {
    for (var n in mockNotificationList) {
      n['isRead'] = true;
    }
    return {'success': true, 'updatedCount': mockNotificationList.length};
  }
}

/// Mock ApiClient that simulates 400 Bad Request / 401 Unauthorized from backend.
class FailingMockApiClient extends MockApiClient {
  final String errorMessage;
  final int statusCode;

  FailingMockApiClient({
    this.errorMessage = 'Tài khoản hoặc mật khẩu không chính xác',
    this.statusCode = 400,
  });

  @override
  Future<Map<String, dynamic>> login({
    required String identifier,
    required String password,
  }) async {
    throw ApiException(
      statusCode: statusCode,
      message: errorMessage,
    );
  }
}
