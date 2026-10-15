import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/widget/bounce_tap.dart';

void main() {
  group('BounceTap Widget Tests', () {
    testWidgets('renders child widget properly', (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: BounceTap(
              child: Text('Click me'),
            ),
          ),
        ),
      );

      expect(find.text('Click me'), findsOneWidget);
      expect(find.byType(AnimatedScale), findsOneWidget);
    });

    testWidgets('scales down on pointer down and scales up on pointer up',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: Scaffold(
            body: Center(
              child: BounceTap(
                scaleFactor: 0.9,
                child: SizedBox(
                  width: 100,
                  height: 50,
                  child: Text('Button'),
                ),
              ),
            ),
          ),
        ),
      );

      final animatedScaleBefore =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(animatedScaleBefore.scale, 1.0);

      // Start gesture
      final gesture = await tester.startGesture(tester.getCenter(find.text('Button')));
      await tester.pump();

      final animatedScalePressed =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(animatedScalePressed.scale, 0.9);

      // End gesture
      await gesture.up();
      await tester.pump();

      final animatedScaleReleased =
          tester.widget<AnimatedScale>(find.byType(AnimatedScale));
      expect(animatedScaleReleased.scale, 1.0);
    });

    testWidgets('triggers onTap callback when tapped',
        (WidgetTester tester) async {
      bool tapped = false;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: BounceTap(
              onTap: () {
                tapped = true;
              },
              child: const Text('Tap target'),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Tap target'));
      await tester.pumpAndSettle();

      expect(tapped, isTrue);
    });

    testWidgets('allows inner ElevatedButton to trigger onPressed when onTap is null',
        (WidgetTester tester) async {
      bool buttonPressed = false;

      await tester.pumpWidget(
        MaterialApp(
          home: Scaffold(
            body: BounceTap(
              child: ElevatedButton(
                onPressed: () {
                  buttonPressed = true;
                },
                child: const Text('Inner Button'),
              ),
            ),
          ),
        ),
      );

      await tester.tap(find.text('Inner Button'));
      await tester.pumpAndSettle();

      expect(buttonPressed, isTrue);
    });
  });
}
