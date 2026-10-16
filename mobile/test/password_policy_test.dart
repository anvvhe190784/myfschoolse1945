import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/util/password_policy.dart';

void main() {
  group('PasswordPolicy Unit Tests', () {
    test('Valid passwords matching all 5 criteria return true', () {
      expect(PasswordPolicy.isValid('Pass@1234'), isTrue);
      expect(PasswordPolicy.isValid('Secure#99'), isTrue);
      expect(PasswordPolicy.isValid('Vanan\$12'), isTrue);
      expect(PasswordPolicy.isValid('Abcd1234!'), isTrue);
    });

    test('Rejects password shorter than 8 characters', () {
      expect(PasswordPolicy.isValid('Pass1!'), isFalse);
      final eval = PasswordPolicy.evaluate('Pass1!');
      expect(eval['length'], isFalse);
    });

    test('Rejects password longer than 12 characters', () {
      expect(PasswordPolicy.isValid('VeryLongPass123!'), isFalse);
      final eval = PasswordPolicy.evaluate('VeryLongPass123!');
      expect(eval['length'], isFalse);
    });

    test('Rejects password missing uppercase letter', () {
      expect(PasswordPolicy.isValid('pass@1234'), isFalse);
      final eval = PasswordPolicy.evaluate('pass@1234');
      expect(eval['uppercase'], isFalse);
      expect(eval['lowercase'], isTrue);
    });

    test('Rejects password missing lowercase letter', () {
      expect(PasswordPolicy.isValid('PASS@1234'), isFalse);
      final eval = PasswordPolicy.evaluate('PASS@1234');
      expect(eval['lowercase'], isFalse);
      expect(eval['uppercase'], isTrue);
    });

    test('Rejects password missing digit', () {
      expect(PasswordPolicy.isValid('Password!@#'), isFalse);
      final eval = PasswordPolicy.evaluate('Password!@#');
      expect(eval['number'], isFalse);
    });

    test('Rejects password missing special character', () {
      expect(PasswordPolicy.isValid('Password123'), isFalse);
      final eval = PasswordPolicy.evaluate('Password123');
      expect(eval['special'], isFalse);
    });
  });
}
