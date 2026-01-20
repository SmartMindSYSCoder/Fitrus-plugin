import 'dart:async';
import 'package:flutter/material.dart';
import 'package:sm_fitrus/fitrus_model.dart';
import 'package:sm_fitrus/sm_fitrus.dart';
import '../theme/app_theme.dart';
import '../widgets/user_input_form.dart';
import '../widgets/connection_status_card.dart';
import '../widgets/results_card.dart';

/// Home Screen - Main screen for Fitrus body fat measurement
class HomeScreen extends StatefulWidget {
  /// Required API key for authentication
  final String apiKey;

  /// Callback to toggle theme
  final VoidCallback onToggleTheme;

  /// Whether dark mode is currently active
  final bool isDarkMode;

  const HomeScreen({
    super.key,
    required this.apiKey,
    required this.onToggleTheme,
    required this.isDarkMode,
  });

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final SmFitrus _smFitrus = SmFitrus();

  FitrusModel _fitrusModel = const FitrusModel();
  StreamSubscription? _subscription;
  bool _isInitialized = false;
  bool _isMeasuring = false;
  bool _hasError = false;
  String? _errorMessage;

  @override
  void dispose() {
    _subscription?.cancel();
    _smFitrus.dispose();
    super.dispose();
  }

  Future<void> _getPermissions() async {
    await _smFitrus.getPermissions();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Permission request sent'),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  Future<void> _initFitrus() async {
    // First subscribe to events
    _subscription?.cancel();
    _subscription = _smFitrus.getEvents().listen((event) {
      debugPrint('Event received: ${event.connectionState}');

      setState(() {
        // Preserve data if the new event is just a status update
        final lastBodyFat = _fitrusModel.bodyFat;
        _fitrusModel = event;

        if (_fitrusModel.bodyFat == null &&
            lastBodyFat != null &&
            !_isMeasuring) {
          _fitrusModel = _fitrusModel.copyWith(bodyFat: lastBodyFat);
        }

        // Check for errors
        if (event.rawConnectionState.toLowerCase().contains('error')) {
          _hasError = true;
          _errorMessage = event.rawConnectionState;
          _isMeasuring = false;
        }

        // Check for results
        if (event.bodyFat != null && event.bodyFat!.bmi > 0) {
          _isMeasuring = false;
          _hasError = false;
        }
      });
    });

    // Then initialize with required API config
    final success = await _smFitrus.init(
      apiKey: widget.apiKey,
    );

    if (success) {
      setState(() {
        _isInitialized = true;
        _hasError = false;
        _errorMessage = null;
      });
    }
  }

  Future<void> _disposeFitrus() async {
    _subscription?.cancel();
    _subscription = null;
    await _smFitrus.dispose();

    setState(() {
      // Preserve the last meaningful body fat result unless explicitly clearing
      final lastBodyFat = _fitrusModel.bodyFat;

      _fitrusModel = FitrusModel(
        connectionState: FitrusConnectionState.disconnected,
        rawConnectionState: 'Disconnected',
        bodyFat: lastBodyFat, // Keep results even after disconnect
      );
      _isInitialized = false;
      _isMeasuring = false;
      _hasError = false;
      _errorMessage = null;
    });
  }

  Future<void> _startMeasurement(UserInputData data) async {
    if (!_isInitialized && !_fitrusModel.isConnected) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please connect to device first'),
          behavior: SnackBarBehavior.floating,
          backgroundColor: AppTheme.accentOrange,
        ),
      );
      return;
    }

    setState(() {
      _isMeasuring = true;
      _hasError = false;
      _errorMessage = null;
      // Reset results
      _fitrusModel = _fitrusModel.copyWith(bodyFat: null);
    });

    await _smFitrus.startBFP(
      heightCm: data.heightCm,
      weightKg: data.weightKg,
      gender: data.gender,
      birth: data.birthString,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: CustomScrollView(
          slivers: [
            // App Bar
            SliverAppBar(
              floating: true,
              backgroundColor: Colors.transparent,
              title: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      gradient: AppTheme.primaryGradient,
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: const Icon(Icons.favorite,
                        color: Colors.white, size: 20),
                  ),
                  const SizedBox(width: 12),
                  const Text('Fitrus'),
                ],
              ),
              actions: [
                IconButton(
                  onPressed: widget.onToggleTheme,
                  icon: Icon(
                      widget.isDarkMode ? Icons.light_mode : Icons.dark_mode),
                  tooltip: widget.isDarkMode ? 'Light Mode' : 'Dark Mode',
                ),
                IconButton(
                  onPressed: _getPermissions,
                  icon: const Icon(Icons.settings),
                  tooltip: 'Permissions',
                ),
                const SizedBox(width: 8),
              ],
            ),

            // Content
            SliverPadding(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 16),
              sliver: SliverList(
                delegate: SliverChildListDelegate([
                  // Connection Status
                  ConnectionStatusCard(
                    fitrusModel: _fitrusModel,
                    isInitialized: _isInitialized,
                    onInit: _initFitrus,
                    onDispose: _disposeFitrus,
                  ),

                  const SizedBox(height: 8),

                  // Results (if available)
                  if (_hasError || (_fitrusModel.bodyFat?.bmi ?? 0) > 0)
                    ResultsCard(
                      bodyFat: _fitrusModel.bodyFat,
                      hasError: _hasError,
                      errorMessage: _errorMessage,
                    ),

                  const SizedBox(height: 8),

                  // User Input Form
                  UserInputForm(
                    onSubmit: _startMeasurement,
                    isLoading: _isMeasuring,
                    isInitialized: _isInitialized,
                    isConnected: _fitrusModel.isConnected,
                  ),

                  const SizedBox(height: 24),
                ]),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// Extension to add copyWith to FitrusModel (if not already available)
extension FitrusModelCopyWith on FitrusModel {
  FitrusModel copyWith({
    bool? isConnected,
    bool? hasData,
    bool? hasProgress,
    FitrusConnectionState? connectionState,
    String? rawConnectionState,
    int? progress,
    BodyFat? bodyFat,
  }) {
    return FitrusModel(
      isConnected: isConnected ?? this.isConnected,
      hasData: hasData ?? this.hasData,
      hasProgress: hasProgress ?? this.hasProgress,
      connectionState: connectionState ?? this.connectionState,
      rawConnectionState: rawConnectionState ?? this.rawConnectionState,
      progress: progress ?? this.progress,
      bodyFat: bodyFat,
    );
  }
}
