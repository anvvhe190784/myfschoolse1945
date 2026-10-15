import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:myfschoolse1945/vn/edu/fpt/controller/verify_email_controller.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/reset_password_screen.dart';
import 'package:myfschoolse1945/vn/edu/fpt/widget/bounce_tap.dart';

class VerifyEmailScreen extends StatefulWidget {
  final String email;
  final VerifyEmailController? controller;

  const VerifyEmailScreen({
    super.key,
    this.email = 'example@email.com',
    this.controller,
  });

  @override
  State<VerifyEmailScreen> createState() => _VerifyEmailScreenState();
}

class _VerifyEmailScreenState extends State<VerifyEmailScreen> {
  late final VerifyEmailController _controller;
  late final bool _ownsController;

  static const Color primaryYellow = Color(0xFFF5A623);
  static const Color labelYellow = Color(0xFFD97706);
  static const Color textDark = Color(0xFF1E293B);

  @override
  void initState() {
    super.initState();
    _ownsController = widget.controller == null;
    _controller = widget.controller ??
        VerifyEmailController(email: widget.email);
    _controller.addListener(_onControllerUpdate);
  }

  void _onControllerUpdate() {
    if (mounted) setState(() {});
  }

  @override
  void dispose() {
    _controller.removeListener(_onControllerUpdate);
    if (_ownsController) {
      _controller.dispose();
    }
    super.dispose();
  }

  void _handleVerifyEmail() async {
    final messenger = ScaffoldMessenger.of(context);
    messenger.hideCurrentSnackBar();

    if (!_controller.isOtpComplete) {
      messenger.showSnackBar(
        const SnackBar(
          content: Text('Vui lòng nhập đầy đủ mã xác thực 6 chữ số'),
        ),
      );
      return;
    }

    final success = await _controller.verifyOtp();
    if (!mounted) return;

    if (success) {
      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (context) => ResetPasswordScreen(
            email: widget.email,
            otp: _controller.currentOtp,
            resetToken: _controller.resetToken,
          ),
        ),
      );
    } else if (_controller.errorMessage != null) {
      messenger.showSnackBar(
        SnackBar(content: Text(_controller.errorMessage!)),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final secondsStr =
        _controller.secondsRemaining.toString().padLeft(2, '0');

    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(28.0, 0.0, 28.0, 72.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                const SizedBox(height: 5),
                Image.asset(
                  'assets/images/fpt_logo.png',
                  height: 100,
                  fit: BoxFit.contain,
                ),
                const SizedBox(height: 48),

                // Mail icon badge
                Container(
                  width: 80,
                  height: 80,
                  decoration: BoxDecoration(
                    color: primaryYellow.withValues(alpha: 0.15),
                    shape: BoxShape.circle,
                  ),
                  child: const Center(
                    child: Icon(
                      Icons.mail_outline_rounded,
                      color: labelYellow,
                      size: 40,
                    ),
                  ),
                ),
                const SizedBox(height: 24),

                // Header
                const Text(
                  'Xác thực email',
                  style: TextStyle(
                    fontSize: 26,
                    fontWeight: FontWeight.bold,
                    color: textDark,
                    letterSpacing: -0.5,
                  ),
                ),
                const SizedBox(height: 8),

                // Subtitle
                Text(
                  'Chúng tôi đã gửi email xác thực đến\n${widget.email}',
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontSize: 14,
                    color: Colors.grey.shade600,
                    height: 1.4,
                  ),
                ),
                const SizedBox(height: 36),

                // OTP Label
                const Align(
                  alignment: Alignment.centerLeft,
                  child: Text(
                    'Nhập mã xác thực 6 chữ số',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: labelYellow,
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // 6 OTP Input Boxes
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: List.generate(
                    VerifyEmailController.otpLength,
                    (index) => _buildOtpBox(index),
                  ),
                ),
                const SizedBox(height: 28),

                // Countdown timer / Resend button
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      'Chưa nhận được mã? ',
                      style: TextStyle(
                        color: Colors.grey.shade600,
                        fontSize: 14,
                      ),
                    ),
                    BounceTap(
                      onTap: _controller.canResend && !_controller.isLoading
                          ? _controller.resendCode
                          : null,
                      child: Text(
                        _controller.canResend
                            ? 'Gửi lại mã'
                            : 'Gửi lại (00:$secondsStr)',
                        style: TextStyle(
                          color: _controller.canResend
                              ? labelYellow
                              : Colors.grey.shade400,
                          fontWeight: FontWeight.bold,
                          fontSize: 14,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 36),

                // Verify Button
                BounceTap(
                  child: SizedBox(
                    width: double.infinity,
                    height: 52,
                    child: ElevatedButton(
                      onPressed: _handleVerifyEmail,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: primaryYellow,
                        foregroundColor: textDark,
                        elevation: 2,
                        shadowColor: primaryYellow.withValues(alpha: 0.4),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      child: _controller.isLoading
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: textDark,
                              ),
                            )
                          : const Text(
                              'Xác thực email',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                    ),
                  ),
                ),
                const SizedBox(height: 24),

                // Back to Sign In
                BounceTap(
                  child: TextButton(
                    onPressed: () {
                      Navigator.of(context).popUntil((route) => route.isFirst);
                    },
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          Icons.arrow_back_rounded,
                          size: 16,
                          color: Colors.grey.shade600,
                        ),
                        const SizedBox(width: 6),
                        Text(
                          'Quay lại đăng nhập',
                          style: TextStyle(
                            color: Colors.grey.shade600,
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildOtpBox(int index) {
    final hasFocus = _controller.focusNodes[index].hasFocus;
    final hasValue = _controller.controllers[index].text.isNotEmpty;

    Color borderColor;
    if (hasFocus) {
      borderColor = primaryYellow;
    } else if (hasValue) {
      borderColor = labelYellow;
    } else {
      borderColor = Colors.grey.shade300;
    }

    return Container(
      width: 48,
      height: 56,
      decoration: BoxDecoration(
        color: hasFocus
            ? primaryYellow.withValues(alpha: 0.08)
            : Colors.grey.shade50,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(
          color: borderColor,
          width: hasFocus ? 2.0 : 1.2,
        ),
      ),
      child: Center(
        child: TextField(
          controller: _controller.controllers[index],
          focusNode: _controller.focusNodes[index],
          keyboardType: TextInputType.number,
          textAlign: TextAlign.center,
          style: const TextStyle(
            fontSize: 22,
            fontWeight: FontWeight.bold,
            color: textDark,
          ),
          inputFormatters: [
            FilteringTextInputFormatter.digitsOnly,
            LengthLimitingTextInputFormatter(2),
          ],
          decoration: const InputDecoration(
            border: InputBorder.none,
            counterText: '',
            contentPadding: EdgeInsets.zero,
          ),
          onChanged: (val) => _controller.handleOtpChanged(val, index, context),
        ),
      ),
    );
  }
}
