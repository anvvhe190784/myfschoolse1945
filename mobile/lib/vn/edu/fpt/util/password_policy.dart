/// Model representing an individual password rule/requirement.
class PasswordRequirement {
  final String id;
  final String label;
  final bool Function(String password) validator;

  const PasswordRequirement({
    required this.id,
    required this.label,
    required this.validator,
  });

  bool isSatisfied(String password) => validator(password);
}

/// Centralized password policy rules and helper validation methods.
class PasswordPolicy {
  static final List<PasswordRequirement> defaultRequirements = [
    PasswordRequirement(
      id: 'length',
      label: 'Độ dài từ 8 - 12 ký tự',
      validator: (p) => p.length >= 8 && p.length <= 12,
    ),
    PasswordRequirement(
      id: 'uppercase',
      label: 'Ít nhất 1 chữ cái in hoa (A-Z)',
      validator: (p) => RegExp(r'[A-Z]').hasMatch(p),
    ),
    PasswordRequirement(
      id: 'lowercase',
      label: 'Ít nhất 1 chữ cái in thường (a-z)',
      validator: (p) => RegExp(r'[a-z]').hasMatch(p),
    ),
    PasswordRequirement(
      id: 'number',
      label: 'Ít nhất 1 chữ số (0-9)',
      validator: (p) => RegExp(r'[0-9]').hasMatch(p),
    ),
    PasswordRequirement(
      id: 'special',
      label: 'Ít nhất 1 ký tự đặc biệt (!@#\$...)',
      validator: (p) => RegExp(r'[^a-zA-Z0-9\s]').hasMatch(p),
    ),
  ];

  /// Checks if [password] meets all 5 core requirements.
  static bool isValid(String password) {
    if (password.isEmpty) return false;
    for (final req in defaultRequirements) {
      if (!req.isSatisfied(password)) {
        return false;
      }
    }
    return true;
  }

  /// Returns a map of requirement id -> satisfied bool.
  static Map<String, bool> evaluate(String password) {
    return {
      for (final req in defaultRequirements)
        req.id: req.isSatisfied(password),
    };
  }
}
