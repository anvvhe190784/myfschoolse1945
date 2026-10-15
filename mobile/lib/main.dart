import 'package:flutter/material.dart';
import 'package:myfschoolse1945/vn/edu/fpt/screen/auth_gate.dart';
import 'package:myfschoolse1945/vn/edu/fpt/service/api_client.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  final Widget? home;
  final ApiClient? apiClient;

  const MyApp({super.key, this.home, this.apiClient});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'F-School',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFFF5A623),
          primary: const Color(0xFFF5A623),
        ),
        useMaterial3: true,
      ),
      home: home ?? AuthGate(apiClient: apiClient),
    );
  }
}

