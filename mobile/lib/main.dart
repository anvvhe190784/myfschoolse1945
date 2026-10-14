import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/login_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'FPT School Login',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFFF5A623),
          primary: const Color(0xFFF5A623),
        ),
        useMaterial3: true,
      ),
      home: const LoginScreen(),
    );
  }
}

