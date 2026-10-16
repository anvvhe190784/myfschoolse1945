import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

/// Controller for VerifyEmailScreen managing OTP inputs, paste logic, and countdown timer.
class VerifyEmailController extends ChangeNotifier {
  static const int otpLength = 6;
  final ApiClient apiClient;
  final String email;

  final List<TextEditingController> controllers;
  final List<FocusNode> focusNodes;
  final bool _ownsControllers;

  Timer? _timer;
  int _secondsRemaining = 45;
  bool _isLoading = false;
  String? _errorMessage;
  String? _resetToken;

  VerifyEmailController({
    required this.email,
    ApiClient? apiClient,
    List<TextEditingController>? controllers,
    List<FocusNode>? focusNodes,
  })  : apiClient = apiClient ?? ApiClient.instance,
        controllers = controllers ??
            List.generate(otpLength, (_) => TextEditingController()),
        focusNodes = focusNodes ??
            List.generate(otpLength, (_) => FocusNode()),
        _ownsControllers = controllers == null && focusNodes == null {
    startTimer();
    for (int i = 0; i < otpLength; i++) {
      this.focusNodes[i].addListener(notifyListeners);
      final index = i;
      this.focusNodes[i].onKeyEvent = (node, event) {
        if (event is KeyDownEvent &&
            event.logicalKey == LogicalKeyboardKey.backspace) {
          if (this.controllers[index].text.isEmpty && index > 0) {
            this.controllers[index - 1].clear();
            this.focusNodes[index - 1].requestFocus();
            notifyListeners();
            return KeyEventResult.handled;
          }
        }
        return KeyEventResult.ignored;
      };
    }
  }

  int get secondsRemaining => _secondsRemaining;
  bool get canResend => _secondsRemaining == 0;
  bool get isLoading => _isLoading;
  String? get errorMessage => _errorMessage;
  String? get resetToken => _resetToken;

  String get currentOtp => controllers.map((c) => c.text).join();
  bool get isOtpComplete => currentOtp.length == otpLength;

  void startTimer() {
    _timer?.cancel();
    _secondsRemaining = 45;
    notifyListeners();

    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_secondsRemaining > 0) {
        _secondsRemaining--;
        notifyListeners();
      } else {
        timer.cancel();
      }
    });
  }

  Future<bool> resendCode() async {
    if (!canResend || _isLoading) return false;
    _isLoading = true;
    for (final controller in controllers) {
      controller.clear();
    }
    _errorMessage = null;
    notifyListeners();

    try {
      await apiClient.forgotPassword(email: email);
      _isLoading = false;
      startTimer();
      return true;
    } catch (e) {
      _isLoading = false;
      _errorMessage =
          e is ApiException ? e.message : 'Không thể gửi lại mã: $e';
      notifyListeners();
      return false;
    }
  }

  void handleOtpChanged(String value, int index, BuildContext? context) {
    if (value.length > 2) {
      _handleOtpPaste(value, context);
      return;
    }

    if (value.length == 2) {
      _handleOverflowInput(value, index, context);
      return;
    }

    _handleSingleOtpInput(value, index, context);
    notifyListeners();
  }

  void _handleOverflowInput(String value, int index, BuildContext? context) {
    if (index < otpLength - 1) {
      controllers[index].text = value[0];
      controllers[index + 1].text = value[1];
      _advanceFocusAfterOverflow(index, context);
    } else {
      controllers[index].text = value[value.length - 1];
      if (context != null) {
        focusNodes[index].unfocus();
      }
    }
    notifyListeners();
  }

  void _advanceFocusAfterOverflow(int index, BuildContext? context) {
    if (context == null) return;
    if (index + 1 < otpLength - 1) {
      focusNodes[index + 2].requestFocus();
    } else {
      focusNodes[index + 1].requestFocus();
    }
  }

  void _handleOtpPaste(String value, BuildContext? context) {
    final digits = value.replaceAll(RegExp(r'\D'), '');
    for (int i = 0; i < otpLength; i++) {
      if (i < digits.length) {
        controllers[i].text = digits[i];
      }
    }
    if (context != null) {
      FocusScope.of(context).unfocus();
    }
    notifyListeners();
  }

  void _handleSingleOtpInput(String value, int index, BuildContext? context) {
    if (context == null) return;

    if (value.isNotEmpty) {
      if (index < otpLength - 1) {
        focusNodes[index + 1].requestFocus();
      } else {
        focusNodes[index].unfocus();
      }
    } else if (index > 0) {
      focusNodes[index - 1].requestFocus();
    }
  }

  Future<bool> verifyOtp() async {
    if (!isOtpComplete) {
      _errorMessage = 'Vui lòng nhập đầy đủ mã xác thực 6 số';
      notifyListeners();
      return false;
    }

    _isLoading = true;
    _errorMessage = null;
    notifyListeners();

    try {
      final res = await apiClient.verifyOtp(email: email, otp: currentOtp);
      _resetToken = res['resetToken'] as String?;
      _isLoading = false;
      notifyListeners();
      return true;
    } catch (e) {
      _isLoading = false;
      _errorMessage =
          e is ApiException ? e.message : 'Mã xác thực không hợp lệ hoặc đã hết hạn: $e';
      notifyListeners();
      return false;
    }
  }

  @override
  void dispose() {
    _timer?.cancel();
    if (_ownsControllers) {
      for (final controller in controllers) {
        controller.dispose();
      }
      for (final focusNode in focusNodes) {
        focusNode.dispose();
      }
    }
    super.dispose();
  }
}
