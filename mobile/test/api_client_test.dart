import 'dart:convert';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

void main() {
  group('ApiClient Tests', () {
    test('Token management and URL formation', () async {
      final client = ApiClient(baseUrl: 'http://10.0.2.2:8080/api/v1');
      expect(client.baseUrl, 'http://10.0.2.2:8080/api/v1');
      expect(client.authToken, isNull);
      expect(client.refreshToken, isNull);
      expect(client.isAuthenticated, isFalse);

      client.setAuthToken('token123', 'refresh123');
      expect(client.authToken, 'token123');
      expect(client.refreshToken, 'refresh123');
      expect(client.isAuthenticated, isTrue);

      await client.clearAuthToken();
      expect(client.authToken, isNull);
      expect(client.refreshToken, isNull);
      expect(client.isAuthenticated, isFalse);
    });

    test('ApiException formatting', () {
      const exception = ApiException(
        statusCode: 401,
        message: 'Unauthorized access',
        details: {'error': 'Invalid credentials'},
      );
      expect(exception.statusCode, 401);
      expect(exception.message, 'Unauthorized access');
      expect(exception.toString(), 'ApiException(401): Unauthorized access');
    });

    test('saveTokens and restoreSession persist and reload tokens', () async {
      final fakeStorage = _InMemorySecureStorage();
      final client = ApiClient(secureStorage: fakeStorage);

      expect(await client.restoreSession(), isNull);

      await client.saveTokens(
        accessToken: 'access-token-999',
        refreshToken: 'refresh-token-888',
      );

      expect(client.authToken, 'access-token-999');
      expect(client.refreshToken, 'refresh-token-888');
      expect(client.isAuthenticated, isTrue);

      // Create a new client instance simulating app relaunch
      final newClient = ApiClient(secureStorage: fakeStorage);
      expect(newClient.authToken, isNull);

      final restoredToken = await newClient.restoreSession();
      expect(restoredToken, 'access-token-999');
      expect(newClient.authToken, 'access-token-999');
      expect(newClient.refreshToken, 'refresh-token-888');
      expect(newClient.isAuthenticated, isTrue);

      await newClient.clearAuthToken();
      expect(newClient.authToken, isNull);
      expect(await newClient.restoreSession(), isNull);
    });

    test('isTokenExpired correctly detects expired and valid JWT tokens', () {
      expect(ApiClient.isTokenExpired('simple-token'), isFalse);

      final futureExp =
          (DateTime.now().add(const Duration(hours: 1)).millisecondsSinceEpoch ~/
              1000);
      final futurePayload = base64Url.encode(
          utf8.encode(jsonEncode({'sub': 'test', 'exp': futureExp})));
      final validJwt = 'header.$futurePayload.signature';
      expect(ApiClient.isTokenExpired(validJwt), isFalse);

      final pastExp = (DateTime.now()
                  .subtract(const Duration(hours: 1))
                  .millisecondsSinceEpoch ~/
              1000);
      final pastPayload = base64Url.encode(
          utf8.encode(jsonEncode({'sub': 'test', 'exp': pastExp})));
      final expiredJwt = 'header.$pastPayload.signature';
      expect(ApiClient.isTokenExpired(expiredJwt), isTrue);
    });
  });
}

class _InMemorySecureStorage extends Fake implements FlutterSecureStorage {
  final Map<String, String> _data = {};

  @override
  Future<void> write({
    required String key,
    required String? value,
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    if (value != null) {
      _data[key] = value;
    }
  }

  @override
  Future<String?> read({
    required String key,
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    return _data[key];
  }

  @override
  Future<void> delete({
    required String key,
    IOSOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    MacOsOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    _data.remove(key);
  }
}
