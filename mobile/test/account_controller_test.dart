import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/account_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';
import 'mock_api_client.dart';

void main() {
  group('AccountController Tests', () {
    test('Default values match student profile requirements', () {
      final controller = AccountController();
      expect(controller.studentName, 'Vũ Văn An');
      expect(controller.studentId, 'HS194500');
      expect(controller.email, 'anvvhe194500@fpt.edu.vn');
      expect(controller.phone, '0987 654 321');
      expect(controller.rollNumber, 'HE194500');
      expect(controller.createdDate, '14/10/2026');
      expect(controller.isLoggingOut, false);
    });

    test('Custom constructor values are preserved', () {
      final controller = AccountController(
        studentName: 'Nguyễn Văn B',
        studentId: 'HS999999',
        email: 'b@fpt.edu.vn',
        phone: '0123 456 789',
        rollNumber: 'HE999999',
        createdDate: '01/01/2026',
      );
      expect(controller.studentName, 'Nguyễn Văn B');
      expect(controller.studentId, 'HS999999');
      expect(controller.email, 'b@fpt.edu.vn');
      expect(controller.phone, '0123 456 789');
      expect(controller.rollNumber, 'HE999999');
      expect(controller.createdDate, '01/01/2026');
    });

    test('logout clears authToken via ApiClient and updates state', () async {
      final apiClient = MockApiClient();
      apiClient.setAuthToken('test-token-12345');
      expect(apiClient.authToken, 'test-token-12345');

      final controller = AccountController(apiClient: apiClient);
      bool notified = false;
      controller.addListener(() {
        notified = true;
      });

      await controller.logout();

      expect(apiClient.authToken, isNull);
      expect(controller.isLoggingOut, false);
      expect(notified, isTrue);
    });

    test('loadProfile does nothing when unauthenticated', () async {
      final apiClient = ApiClient();
      expect(apiClient.isAuthenticated, isFalse);

      final controller = AccountController(apiClient: apiClient);
      await controller.loadProfile();

      expect(controller.studentName, 'Vũ Văn An');
      expect(controller.email, 'anvvhe194500@fpt.edu.vn');
      expect(controller.phone, '0987 654 321');
      expect(controller.rollNumber, 'HE194500');
    });

    test('loadProfile updates fields from backend payload including phoneNumber', () async {
      final mockApiClient = _FakeAuthenticatedApiClient({
        'userId': 99,
        'rollNumber': 'HE190784',
        'email': 'vananvu429@gmail.com',
        'fullName': 'Vũ Văn An Real',
        'phoneNumber': '0912345678',
      });

      final controller = AccountController(apiClient: mockApiClient);
      await controller.loadProfile();

      expect(controller.studentName, 'Vũ Văn An Real');
      expect(controller.email, 'vananvu429@gmail.com');
      expect(controller.rollNumber, 'HE190784');
      expect(controller.phone, '0912345678');
      expect(controller.studentId, 'HE190784');
    });
  });
}

class _FakeAuthenticatedApiClient extends ApiClient {
  final Map<String, dynamic> mockProfile;

  _FakeAuthenticatedApiClient(this.mockProfile);

  @override
  bool get isAuthenticated => true;

  @override
  Future<Map<String, dynamic>> getCurrentUser() async => mockProfile;
}
