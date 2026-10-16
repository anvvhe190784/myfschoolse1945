import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/util/password_policy.dart';

/// Interactive checklist displaying password requirements with real-time feedback.
/// Displays neutral gray when password is empty, and green/red feedback once typed.
class PasswordCriteriaChecklist extends StatelessWidget {
  final String password;
  final String? oldPassword;
  final bool showOldPasswordCheck;

  const PasswordCriteriaChecklist({
    super.key,
    required this.password,
    this.oldPassword,
    this.showOldPasswordCheck = false,
  });

  @override
  Widget build(BuildContext context) {
    final results = PasswordPolicy.evaluate(password);
    final isPasswordEmpty = password.isEmpty;

    final items = <Widget>[];

    for (final req in PasswordPolicy.defaultRequirements) {
      final isMet = results[req.id] ?? false;
      items.add(
        _buildCheckItem(
          req.label,
          isMet,
          isNeutral: isPasswordEmpty,
        ),
      );
    }

    if (showOldPasswordCheck) {
      final currentOld = oldPassword ?? '';
      final isNeutral = isPasswordEmpty || currentOld.isEmpty;
      final isDifferentFromOld = !isNeutral && password != currentOld;

      items.add(
        _buildCheckItem(
          'Khác mật khẩu hiện tại',
          isDifferentFromOld,
          isNeutral: isNeutral,
        ),
      );
    }

    return Container(
      width: double.infinity,
      margin: const EdgeInsets.only(top: 10, bottom: 6),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: const Color(0xFFE2E8F0), width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Điều kiện mật khẩu:',
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: Color(0xFF475569),
            ),
          ),
          const SizedBox(height: 8),
          ...items,
        ],
      ),
    );
  }

  Widget _buildCheckItem(
    String label,
    bool isMet, {
    bool isNeutral = false,
  }) {
    final Color iconColor;
    final IconData iconData;
    final Color textColor;

    if (isNeutral) {
      iconColor = const Color(0xFF94A3B8); // Muted gray when empty
      iconData = Icons.radio_button_unchecked;
      textColor = const Color(0xFF64748B);
    } else if (isMet) {
      iconColor = const Color(0xFF16A34A); // Green satisfied
      iconData = Icons.check_circle_rounded;
      textColor = const Color(0xFF15803D);
    } else {
      iconColor = const Color(0xFFDC2626); // Red unsatisfied
      iconData = Icons.cancel_outlined;
      textColor = const Color(0xFFDC2626);
    }

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3.0),
      child: Row(
        children: [
          Icon(
            iconData,
            size: 16,
            color: iconColor,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              label,
              style: TextStyle(
                fontSize: 12,
                color: textColor,
                fontWeight: isMet ? FontWeight.w500 : FontWeight.normal,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
