import 'dart:async';
import 'dart:convert';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:flutter/services.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:sm_fitrus/fitrus_model.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

/// Fitrus Gender Enum
enum FitrusGender {
  male,
  female;

  /// Converts to API format ('M' or 'F')
  String toApiFormat() {
    switch (this) {
      case FitrusGender.male:
        return 'M';
      case FitrusGender.female:
        return 'F';
    }
  }
}

/// SmFitrus Plugin
///
/// Provides body fat measurement functionality using the Fitrus device.
class SmFitrus {
  final MethodChannel _methodChannel = const MethodChannel('sm_fitrus');
  final EventChannel _eventChannel = const EventChannel("sm_fitrus_status");

  /// Stream of [FitrusModel] events from the native side.
  /// Use this to listen for connection updates, progress, and measurement results.
  Stream<FitrusModel> getEvents() {
    return _eventChannel.receiveBroadcastStream().map((event) {
      try {
        final String jsonString = event.toString();
        final Map<String, dynamic> jsonMap = jsonDecode(jsonString);
        return FitrusModel.fromJson(jsonMap);
      } catch (e, stack) {
        debugPrint("Error parsing Fitrus event: $e\n$stack");
        return const FitrusModel(
          connectionState: FitrusConnectionState.unknown,
          rawConnectionState: 'Parse Error',
        );
      }
    });
  }

  Future<void> getPermissions() async {
    try {
      await _methodChannel.invokeMethod('getPermissions');
    } on PlatformException catch (e) {
      debugPrint("Failed to get permissions: '${e.message}'.");
    }
  }

  /// Initializes the Fitrus plugin.
  ///
  /// **Required parameters:**
  /// - [apiKey] The API key for authentication.
  ///
  /// **Validation:**
  /// - Returns false and shows toast if Bluetooth is not enabled.
  /// - Returns false and shows toast if Internet is not connected.
  /// - Returns false and shows toast if apiKey is empty.
  Future<bool> init({
    required String apiKey,
  }) async {
    // Validate apiKey
    if (apiKey.isEmpty) {
      _showToast('API Key is required');
      debugPrint('SmFitrus: init() failed - API Key is required');
      return false;
    }

    // Validate Bluetooth
    try {
      final isBluetoothOn = await FlutterBluePlus.adapterState.first;
      if (isBluetoothOn != BluetoothAdapterState.on) {
        _showToast('Bluetooth is not enabled. Please enable Bluetooth.');
        debugPrint('SmFitrus: init() failed - Bluetooth not enabled');
        return false;
      }
    } catch (e) {
      debugPrint('SmFitrus: Failed to check Bluetooth state: $e');
      // Continue anyway, native side will also check
    }

    // Validate Internet
    try {
      final connectivity = await Connectivity().checkConnectivity();
      if (connectivity.contains(ConnectivityResult.none)) {
        _showToast('Internet connection is required');
        debugPrint('SmFitrus: init() failed - No internet connection');
        return false;
      }
    } catch (e) {
      debugPrint('SmFitrus: Failed to check connectivity: $e');
      // Continue anyway, native side will also check
    }

    // All validations passed - call native init
    try {
      final Map<String, String> args = {
        'apiKey': apiKey,
      };
      await _methodChannel.invokeMethod('init', args);
      return true;
    } on PlatformException catch (e) {
      _showToast('Failed to initialize: ${e.message}');
      debugPrint("Failed to initialize Fitrus: '${e.message}'.");
      return false;
    }
  }

  Future<void> dispose() async {
    try {
      await _methodChannel.invokeMethod('dispose');
    } on PlatformException catch (e) {
      debugPrint("Failed to dispose Fitrus: '${e.message}'.");
    }
  }

  /// Starts Body Fat Percentage (BFP) measurement.
  ///
  /// **Required parameters:**
  /// - [heightCm] Height in centimeters (must be > 0).
  /// - [weightKg] Weight in kilograms (must be > 0).
  /// - [gender] Gender (male or female).
  /// - [birth] Birth date in 'yyyyMMdd' format (e.g., '19991203').
  ///
  /// **Validation:**
  /// - Returns false and shows toast if any parameter is invalid.
  Future<bool> startBFP({
    required double heightCm,
    required double weightKg,
    required FitrusGender gender,
    required String birth,
  }) async {
    // Validate heightCm
    if (heightCm <= 0) {
      _showToast('Invalid height. Please enter a valid height in cm.');
      debugPrint('SmFitrus: startBFP() failed - Invalid height: $heightCm');
      return false;
    }
    if (heightCm > 300) {
      _showToast('Height cannot exceed 300 cm');
      debugPrint('SmFitrus: startBFP() failed - Height too high: $heightCm');
      return false;
    }

    // Validate weightKg
    if (weightKg <= 0) {
      _showToast('Invalid weight. Please enter a valid weight in kg.');
      debugPrint('SmFitrus: startBFP() failed - Invalid weight: $weightKg');
      return false;
    }
    if (weightKg > 500) {
      _showToast('Weight cannot exceed 500 kg');
      debugPrint('SmFitrus: startBFP() failed - Weight too high: $weightKg');
      return false;
    }

    // Validate birth format (yyyyMMdd)
    if (birth.length != 8) {
      _showToast('Invalid birth date. Use yyyyMMdd format (e.g., 19901203)');
      debugPrint('SmFitrus: startBFP() failed - Invalid birth length: $birth');
      return false;
    }
    final birthInt = int.tryParse(birth);
    if (birthInt == null) {
      _showToast('Invalid birth date. Use yyyyMMdd format (e.g., 19901203)');
      debugPrint('SmFitrus: startBFP() failed - Birth not numeric: $birth');
      return false;
    }

    // Validate birth date is reasonable
    final year = int.tryParse(birth.substring(0, 4)) ?? 0;
    final month = int.tryParse(birth.substring(4, 6)) ?? 0;
    final day = int.tryParse(birth.substring(6, 8)) ?? 0;
    if (year < 1900 || year > DateTime.now().year) {
      _showToast('Invalid birth year');
      debugPrint('SmFitrus: startBFP() failed - Invalid year: $year');
      return false;
    }
    if (month < 1 || month > 12) {
      _showToast('Invalid birth month');
      debugPrint('SmFitrus: startBFP() failed - Invalid month: $month');
      return false;
    }
    if (day < 1 || day > 31) {
      _showToast('Invalid birth day');
      debugPrint('SmFitrus: startBFP() failed - Invalid day: $day');
      return false;
    }

    // All validations passed - call native startBFP
    final Map<String, String> args = {
      'height': heightCm.toStringAsFixed(1),
      'weight': weightKg.toStringAsFixed(1),
      'gender': gender.toApiFormat(),
      'birth': birth,
    };

    try {
      await _methodChannel.invokeMethod('startBFP', args);
      return true;
    } on PlatformException catch (e) {
      _showToast('Failed to start measurement: ${e.message}');
      debugPrint("Failed to start BFP measurement: '${e.message}'.");
      return false;
    }
  }

  /// Shows a toast message to the user
  void _showToast(String message) {
    Fluttertoast.showToast(
      msg: message,
      toastLength: Toast.LENGTH_LONG,
      gravity: ToastGravity.BOTTOM,
      backgroundColor: const Color(0xFFE74C3C),
      textColor: Colors.white,
      fontSize: 14.0,
    );
  }
}
