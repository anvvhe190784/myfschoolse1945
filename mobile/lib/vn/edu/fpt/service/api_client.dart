import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Exception thrown when an API request fails.
class ApiException implements Exception {
  final int statusCode;
  final String message;
  final dynamic details;

  const ApiException({
    required this.statusCode,
    required this.message,
    this.details,
  });

  @override
  String toString() => 'ApiException($statusCode): $message';
}

/// HTTP API Client for interacting with the Spring Boot Backend (Web API).
/// Automatically connects to 10.0.2.2 on Android Emulator and localhost on Desktop/Web.
class ApiClient {
  static ApiClient? _instance;
  static ApiClient get instance => _instance ??= ApiClient();
  static set instance(ApiClient? client) => _instance = client;

  static const String tokenKey = 'jwt_access_token';
  static const String legacyTokenKey = 'jwt_token';
  static const String refreshTokenKey = 'jwt_refresh_token';

  final String baseUrl;
  final HttpClient _httpClient;
  final FlutterSecureStorage _secureStorage;
  String? _authToken;
  String? _refreshToken;
  Completer<bool>? _refreshCompleter;

  ApiClient({
    String? baseUrl,
    HttpClient? httpClient,
    FlutterSecureStorage? secureStorage,
  })  : baseUrl = baseUrl ?? _resolveDefaultBaseUrl(),
        _httpClient = (httpClient ?? HttpClient())
          ..connectionTimeout = const Duration(seconds: 5),
        _secureStorage = secureStorage ??
            const FlutterSecureStorage(
              aOptions: AndroidOptions(encryptedSharedPreferences: true),
            );

  static String _resolveDefaultBaseUrl() {
    if (kIsWeb) {
      return 'http://localhost:8080/api/v1';
    }
    try {
      if (Platform.isAndroid) {
        return 'http://10.0.2.2:8080/api/v1';
      }
    } catch (_) {
      // Fallback for non-platform environments
    }
    return 'http://localhost:8080/api/v1';
  }

  void setAuthToken(String token, [String? refreshToken]) {
    _authToken = token;
    if (refreshToken != null) {
      _refreshToken = refreshToken;
    }
  }

  Future<void> saveTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    _authToken = accessToken;
    _refreshToken = refreshToken;
    try {
      await _secureStorage
          .write(key: tokenKey, value: accessToken)
          .timeout(const Duration(seconds: 5));
      if (refreshToken.isNotEmpty) {
        await _secureStorage
            .write(key: refreshTokenKey, value: refreshToken)
            .timeout(const Duration(seconds: 5));
      }
    } catch (_) {}
  }

  Future<void> saveAuthToken(String token) async {
    await saveTokens(accessToken: token, refreshToken: _refreshToken ?? '');
  }

  Future<String?> restoreSession() async {
    try {
      final savedToken = await _secureStorage
          .read(key: tokenKey)
          .timeout(const Duration(seconds: 5));
      final legacyToken = savedToken ??
          await _secureStorage
              .read(key: legacyTokenKey)
              .timeout(const Duration(seconds: 5));
      final savedRefreshToken = await _secureStorage
          .read(key: refreshTokenKey)
          .timeout(const Duration(seconds: 5));

      if (savedRefreshToken != null && savedRefreshToken.isNotEmpty) {
        _refreshToken = savedRefreshToken;
      }

      if (legacyToken != null && legacyToken.isNotEmpty) {
        _authToken = legacyToken;
        return legacyToken;
      } else if (_refreshToken != null && _refreshToken!.isNotEmpty) {
        // Access token missing/expired, attempt transparent refresh
        final success = await refreshAccessToken();
        if (success) return _authToken;
      }
    } catch (_) {}
    return null;
  }

  Future<void> clearAuthToken() async {
    _authToken = null;
    _refreshToken = null;
    try {
      await _secureStorage
          .delete(key: tokenKey)
          .timeout(const Duration(seconds: 5));
      await _secureStorage
          .delete(key: legacyTokenKey)
          .timeout(const Duration(seconds: 5));
      await _secureStorage
          .delete(key: refreshTokenKey)
          .timeout(const Duration(seconds: 5));
    } catch (_) {}
  }

  String? get authToken => _authToken;
  String? get refreshToken => _refreshToken;
  bool get isAuthenticated => _authToken != null && _authToken!.isNotEmpty;

  Map<String, String> _buildHeaders() {
    final headers = <String, String>{
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };
    if (_authToken != null && _authToken!.isNotEmpty) {
      headers['Authorization'] = 'Bearer $_authToken';
    }
    return headers;
  }

  Uri _buildUri(String path, [Map<String, dynamic>? queryParams]) {
    final cleanPath = path.startsWith('/') ? path : '/$path';
    final fullUrl = '$baseUrl$cleanPath';
    final uri = Uri.parse(fullUrl);
    if (queryParams != null && queryParams.isNotEmpty) {
      return uri.replace(
        queryParameters: queryParams.map(
          (key, value) => MapEntry(key, value.toString()),
        ),
      );
    }
    return uri;
  }

  /// Concurrency-safe transparent token refresh using Refresh Token (RTR)
  Future<bool> refreshAccessToken() async {
    if (_refreshToken == null || _refreshToken!.isEmpty) {
      return false;
    }

    if (_refreshCompleter != null) {
      return _refreshCompleter!.future;
    }

    final completer = Completer<bool>();
    _refreshCompleter = completer;

    try {
      final uri = _buildUri('/auth/refresh-token');
      final request = await _httpClient.postUrl(uri);
      request.headers.set('Content-Type', 'application/json');
      request.headers.set('Accept', 'application/json');
      request.write(jsonEncode({'refreshToken': _refreshToken}));

      final response = await request.close();
      final bodyStr = await response.transform(utf8.decoder).join();

      if (response.statusCode >= 200 && response.statusCode < 300) {
        final decoded = jsonDecode(bodyStr);
        if (decoded is Map<String, dynamic>) {
          final newAccess = decoded['accessToken'] ?? decoded['access_token'] ?? decoded['token'];
          final newRefresh = decoded['refreshToken'] ?? decoded['refresh_token'];
          if (newAccess != null) {
            await saveTokens(
              accessToken: newAccess.toString(),
              refreshToken: (newRefresh ?? _refreshToken).toString(),
            );
            completer.complete(true);
            return true;
          }
        }
      }

      await clearAuthToken();
      completer.complete(false);
      return false;
    } catch (_) {
      completer.complete(false);
      return false;
    } finally {
      _refreshCompleter = null;
    }
  }

  Future<dynamic> get(String path, {Map<String, dynamic>? queryParams, bool isRetry = false}) async {
    final uri = _buildUri(path, queryParams);
    final request = await _httpClient.getUrl(uri);
    _buildHeaders().forEach((key, value) {
      request.headers.set(key, value);
    });

    final response = await request.close();
    final body = await response.transform(utf8.decoder).join();

    if (response.statusCode == 401 && !isRetry && !path.contains('/auth/')) {
      final refreshed = await refreshAccessToken();
      if (refreshed) {
        return get(path, queryParams: queryParams, isRetry: true);
      }
    }

    return _handleResponse(response.statusCode, body);
  }

  Future<dynamic> post(String path, {dynamic body, bool isRetry = false}) async {
    final uri = _buildUri(path);
    final request = await _httpClient.postUrl(uri);
    _buildHeaders().forEach((key, value) {
      request.headers.set(key, value);
    });

    if (body != null) {
      final jsonBody = jsonEncode(body);
      request.headers.contentType = ContentType.json;
      request.write(jsonBody);
    }

    final response = await request.close();
    final bodyStr = await response.transform(utf8.decoder).join();

    if (response.statusCode == 401 && !isRetry && !path.contains('/auth/')) {
      final refreshed = await refreshAccessToken();
      if (refreshed) {
        return post(path, body: body, isRetry: true);
      }
    }

    return _handleResponse(response.statusCode, bodyStr);
  }

  Future<dynamic> patch(String path, {dynamic body, bool isRetry = false}) async {
    final uri = _buildUri(path);
    final request = await _httpClient.patchUrl(uri);
    _buildHeaders().forEach((key, value) {
      request.headers.set(key, value);
    });

    if (body != null) {
      final jsonBody = jsonEncode(body);
      request.headers.contentType = ContentType.json;
      request.write(jsonBody);
    }

    final response = await request.close();
    final bodyStr = await response.transform(utf8.decoder).join();

    if (response.statusCode == 401 && !isRetry && !path.contains('/auth/')) {
      final refreshed = await refreshAccessToken();
      if (refreshed) {
        return patch(path, body: body, isRetry: true);
      }
    }

    return _handleResponse(response.statusCode, bodyStr);
  }

  dynamic _handleResponse(int statusCode, String body) {
    dynamic decoded;
    if (body.isNotEmpty) {
      try {
        decoded = jsonDecode(body);
      } catch (_) {
        decoded = body;
      }
    }

    if (statusCode >= 200 && statusCode < 300) {
      return decoded;
    }

    String message = 'Request failed with status $statusCode';
    if (decoded is Map<String, dynamic>) {
      if (decoded['detail'] != null) {
        message = decoded['detail'].toString();
      } else if (decoded['message'] != null) {
        message = decoded['message'].toString();
      } else if (decoded['title'] != null) {
        message = decoded['title'].toString();
      }
    }

    throw ApiException(
      statusCode: statusCode,
      message: message,
      details: decoded,
    );
  }

  // --- Specific Backend Web API Endpoints ---

  /// POST /api/v1/auth/login
  Future<Map<String, dynamic>> login({
    required String identifier,
    required String password,
  }) async {
    final result = await post('/auth/login', body: {
      'identifier': identifier,
      'password': password,
    });
    if (result is Map<String, dynamic>) {
      final access = result['accessToken'] ?? result['access_token'] ?? result['token'];
      final refresh = result['refreshToken'] ?? result['refresh_token'];
      if (access != null) {
        await saveTokens(
          accessToken: access.toString(),
          refreshToken: refresh?.toString() ?? '',
        );
      }
      return result;
    }
    throw const ApiException(statusCode: 500, message: 'Invalid response format');
  }

  /// POST /api/v1/auth/logout
  Future<void> logout() async {
    try {
      if (_authToken != null || _refreshToken != null) {
        await post('/auth/logout', body: {
          if (_refreshToken != null && _refreshToken!.isNotEmpty)
            'refreshToken': _refreshToken,
        });
      }
    } catch (_) {
      // Ignore network errors on logout to ensure local purge succeeds
    } finally {
      await clearAuthToken();
    }
  }

  /// GET /api/v1/auth/me
  Future<Map<String, dynamic>> getCurrentUser() async {
    final result = await get('/auth/me');
    if (result is Map<String, dynamic>) {
      return result;
    }
    throw const ApiException(statusCode: 500, message: 'Invalid user response');
  }

  /// POST /api/v1/auth/forgot-password
  Future<Map<String, dynamic>> forgotPassword({
    required String email,
  }) async {
    final result = await post('/auth/forgot-password', body: {
      'email': email,
    });
    if (result is Map<String, dynamic>) {
      return result;
    }
    return {'message': 'Mã xác thực OTP đã được gửi đến email của bạn.', 'success': true};
  }

  /// POST /api/v1/auth/verify-otp
  Future<Map<String, dynamic>> verifyOtp({
    required String email,
    required String otp,
  }) async {
    final result = await post('/auth/verify-otp', body: {
      'email': email,
      'otp': otp,
    });
    if (result is Map<String, dynamic>) {
      return result;
    }
    return {'message': 'Xác thực OTP thành công!', 'success': true};
  }

  /// POST /api/v1/auth/reset-password
  Future<Map<String, dynamic>> resetPassword({
    required String email,
    required String otp,
    required String newPassword,
  }) async {
    final result = await post('/auth/reset-password', body: {
      'email': email,
      'otp': otp,
      'newPassword': newPassword,
    });
    if (result is Map<String, dynamic>) {
      return result;
    }
    return {'message': 'Password reset successful', 'success': true};
  }

  /// POST /api/v1/auth/change-password
  Future<Map<String, dynamic>> changePassword({
    required String oldPassword,
    required String newPassword,
  }) async {
    final result = await post('/auth/change-password', body: {
      'oldPassword': oldPassword,
      'newPassword': newPassword,
    });
    if (result is Map<String, dynamic>) {
      return result;
    }
    return {'message': 'Đổi mật khẩu thành công!', 'success': true};
  }

  /// GET /api/v1/news
  Future<List<dynamic>> getNews({int? limit}) async {
    final queryParams = limit != null ? {'limit': limit.toString()} : null;
    final result = await get('/news', queryParams: queryParams);
    if (result is List) {
      return result;
    }
    return [];
  }

  /// GET /api/v1/news/{id}
  Future<Map<String, dynamic>> getNewsDetail(int id) async {
    final result = await get('/news/$id');
    return result as Map<String, dynamic>;
  }

  /// GET /api/v1/schedules
  Future<List<dynamic>> getSchedules({String? date}) async {
    final query = date != null ? {'date': date} : null;
    final result = await get('/schedules', queryParams: query);
    if (result is List) {
      return result;
    }
    return [];
  }

  /// GET /api/v1/grades
  Future<List<dynamic>> getGrades({int? classId}) async {
    final query = classId != null ? {'classId': classId} : null;
    final result = await get('/grades', queryParams: query);
    if (result is List) {
      return result;
    }
    return [];
  }

  /// GET /api/v1/requests
  Future<List<dynamic>> getRequests() async {
    final result = await get('/requests');
    if (result is List) {
      return result;
    }
    return [];
  }

  /// POST /api/v1/requests
  Future<Map<String, dynamic>> createRequest(Map<String, dynamic> data) async {
    final result = await post('/requests', body: data);
    return result as Map<String, dynamic>;
  }

  /// GET /api/v1/clubs
  Future<List<dynamic>> getClubs() async {
    final result = await get('/clubs');
    if (result is List) {
      return result;
    }
    return [];
  }

  /// GET /api/v1/clubs/{id}
  Future<Map<String, dynamic>> getClubDetail(int id) async {
    final result = await get('/clubs/$id');
    return result as Map<String, dynamic>;
  }

  /// GET /api/v1/notifications
  Future<dynamic> getNotifications({
    int page = 0,
    int size = 10,
    bool unreadOnly = false,
  }) async {
    final result = await get('/notifications', queryParams: {
      'page': page.toString(),
      'size': size.toString(),
      'unreadOnly': unreadOnly.toString(),
    });
    return result;
  }

  /// GET /api/v1/notifications/unread-count
  Future<int> getUnreadNotificationCount() async {
    try {
      final result = await get('/notifications/unread-count');
      if (result is Map<String, dynamic> && result['unreadCount'] != null) {
        return (result['unreadCount'] as num).toInt();
      }
      return 0;
    } catch (_) {
      return 0;
    }
  }

  /// PATCH /api/v1/notifications/{id}/read
  Future<Map<String, dynamic>> markNotificationRead(int id) async {
    final result = await patch('/notifications/$id/read');
    return result is Map<String, dynamic> ? result : {};
  }

  /// PATCH /api/v1/notifications/read-all
  Future<Map<String, dynamic>> markAllNotificationsRead() async {
    final result = await patch('/notifications/read-all');
    return result is Map<String, dynamic> ? result : {};
  }
}
