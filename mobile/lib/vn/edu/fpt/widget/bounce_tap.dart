import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// A lightweight micro-interaction widget that scales down slightly when pressed
/// and springs back upon release, accompanied by subtle haptic feedback.
class BounceTap extends StatefulWidget {
  final Widget child;
  final VoidCallback? onTap;
  final double scaleFactor;
  final Duration duration;
  final bool enableHaptic;

  const BounceTap({
    super.key,
    required this.child,
    this.onTap,
    this.scaleFactor = 0.95,
    this.duration = const Duration(milliseconds: 100),
    this.enableHaptic = true,
  });

  @override
  State<BounceTap> createState() => _BounceTapState();
}

class _BounceTapState extends State<BounceTap> {
  bool _isPressed = false;

  void _onPointerDown(PointerDownEvent event) {
    if (!mounted) return;
    setState(() => _isPressed = true);
    if (widget.enableHaptic) {
      HapticFeedback.lightImpact();
    }
  }

  void _onPointerUp(PointerUpEvent event) {
    if (!mounted) return;
    setState(() => _isPressed = false);
  }

  void _onPointerCancel(PointerCancelEvent event) {
    if (!mounted) return;
    setState(() => _isPressed = false);
  }

  @override
  Widget build(BuildContext context) {
    final animatedChild = AnimatedScale(
      scale: _isPressed ? widget.scaleFactor : 1.0,
      duration: widget.duration,
      curve: Curves.easeOutCubic,
      child: widget.child,
    );

    final listenerWidget = Listener(
      behavior: HitTestBehavior.translucent,
      onPointerDown: _onPointerDown,
      onPointerUp: _onPointerUp,
      onPointerCancel: _onPointerCancel,
      child: animatedChild,
    );

    if (widget.onTap != null) {
      return GestureDetector(
        behavior: HitTestBehavior.opaque,
        onTap: widget.onTap,
        child: listenerWidget,
      );
    }

    return listenerWidget;
  }
}
