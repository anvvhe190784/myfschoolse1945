import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

/// Controller for AccountScreen managing student profile and authentication state.
class AccountController extends ChangeNotifier {
  final ApiClient apiClient;

  String studentName;
  String studentId;
  String email;
  String phone;
  String rollNumber;
  String createdDate;
  bool _isLoading = false;
  bool _isLoggingOut = false;

  AccountController({
    ApiClient? apiClient,
    this.studentName = 'Vũ Văn An',
    this.studentId = 'HS194500',
    this.email = 'anvvhe194500@fpt.edu.vn',
    this.phone = '0987 654 321',
    this.rollNumber = 'HE194500',
    this.createdDate = '14/10/2026',
  }) : apiClient = apiClient ?? ApiClient.instance;

  bool get isLoading => _isLoading;
  bool get isLoggingOut => _isLoggingOut;

  void updateProfile({
    String? studentName,
    String? studentId,
    String? email,
    String? phone,
    String? rollNumber,
    String? createdDate,
  }) {
    if (studentName != null) this.studentName = studentName;
    if (studentId != null) this.studentId = studentId;
    if (email != null) this.email = email;
    if (phone != null) this.phone = phone;
    if (rollNumber != null) this.rollNumber = rollNumber;
    if (createdDate != null) this.createdDate = createdDate;
    notifyListeners();
  }

  /// Load current user profile from Backend Web API `GET /api/v1/auth/me`.
  Future<void> loadProfile() async {
    if (!apiClient.isAuthenticated) return;
    _isLoading = true;
    notifyListeners();
    try {
      final data = await apiClient.getCurrentUser();
      studentName = data['fullName']?.toString() ?? studentName;
      email = data['email']?.toString() ?? email;
      rollNumber = data['rollNumber']?.toString() ?? rollNumber;
      final phoneVal = data['phoneNumber'] ?? data['phone'];
      if (phoneVal != null && phoneVal.toString().isNotEmpty) {
        phone = phoneVal.toString();
      }
      final idVal = data['studentId'] ?? data['rollNumber'] ?? data['userId'];
      if (idVal != null && idVal.toString().isNotEmpty) {
        studentId = idVal.toString();
      }
    } catch (_) {
      // Keep existing/cached fallback data if backend call fails
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  /// Perform logout: revokes token on server (Redis Blacklist) and cleans up secure storage.
  Future<void> logout() async {
    _isLoggingOut = true;
    notifyListeners();

    try {
      await apiClient.logout();
    } finally {
      _isLoggingOut = false;
      notifyListeners();
    }
  }
}
