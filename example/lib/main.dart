import 'package:flutter/material.dart';
import 'theme/app_theme.dart';
import 'screens/home_screen.dart';

const String kFitrusApiKey =
    'vrmCquCRjqTKGQNt3b9pEYy6NhjOL45Mi3d56I16RGTuCAeDNXW53kDaJGn7KUii5SAnHAdtcNoIlnJUk5M5HIj3mJpKAzsIIDilz0bKwdIekWot5X1KyCBMUXBGmICS'; // TODO: Enter your API Key here

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const FitrusApp());
}

/// Fitrus Example App
///
/// This app demonstrates the sm_fitrus plugin with a modern UI.
/// Supports both Light and Dark modes.
class FitrusApp extends StatefulWidget {
  const FitrusApp({super.key});

  @override
  State<FitrusApp> createState() => _FitrusAppState();
}

class _FitrusAppState extends State<FitrusApp> {
  // Default to dark mode
  ThemeMode _themeMode = ThemeMode.dark;

  void _toggleTheme() {
    setState(() {
      _themeMode =
          _themeMode == ThemeMode.dark ? ThemeMode.light : ThemeMode.dark;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Fitrus Body Composition',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      themeMode: _themeMode,
      home: HomeScreen(
        apiKey: kFitrusApiKey,
        onToggleTheme: _toggleTheme,
        isDarkMode: _themeMode == ThemeMode.dark,
      ),
    );
  }
}
