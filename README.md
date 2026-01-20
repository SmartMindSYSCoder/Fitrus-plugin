# sm_fitrus

A Flutter plugin for the Fitrus Body Composition device.

## Features

- **Connect** to Fitrus devices via BLE.
- **Measure** Body Fat Percentage (BFP), Skeletal Muscle Mass, Basal Metabolic Rate (BMR), and more.
- **Calculate** BMI and Water Percentage automatically (even if the device API doesn't return them).
- **Stream** real-time connection status and measurement progress.

## Prerequisites

### Android

This plugin requires the following permissions to be granted at runtime:
- `BLUETOOTH_SCAN`
- `BLUETOOTH_CONNECT`
- `ACCESS_FINE_LOCATION` (Required for BLE scanning on Android 11 and below)

**Important**: 
- **Internet Connection** is required for the device to calculate body composition analysis via the Fitrus API.
- **Bluetooth** must be enabled on the device.
- **Location Services** (GPS) must be enabled for scanning to work on Android.

## Getting Started

1. **Add dependency** to your `pubspec.yaml`:

```yaml
dependencies:
  sm_fitrus:
    git: https://github.com/SmartMindSYSCoder/Fitrus-plugin.git
```

2. **Import** the package:

```dart
import 'package:sm_fitrus/sm_fitrus.dart';
```

3. **Initialize** the plugin with your API Key:

```dart
final smFitrus = SmFitrus();

// Initialize with your API Key
// Note: API URL is no longer required and defaults to the standard Fitrus API.
await smFitrus.init(
  apiKey: 'YOUR_API_KEY_HERE'
);
```

4. **Request Permissions**:

```dart
await smFitrus.getPermissions();
```

5. **Listen for Events** (Connection State & Results):

```dart
StreamBuilder<FitrusModel>(
  stream: smFitrus.getEvents(),
  builder: (context, snapshot) {
    if (!snapshot.hasData) return Text("Waiting...");
    
    final state = snapshot.data!;
    
    if (state.connectionState == FitrusConnectionState.connected) {
      return Text("Connected!");
    }
    
    if (state.hasData && state.bodyFat != null) {
      final bodyFat = state.bodyFat!;
      return Column(children: [
        Text("Body Fat: ${bodyFat.fatPercentage}%"),
        Text("Muscle Mass: ${bodyFat.muscleMass} kg"),
        Text("BMI: ${bodyFat.bmi}"),
        Text("Water: ${bodyFat.waterPercentage}%"),
        Text("Minerals: ${bodyFat.minerals} kg"),
      ]);
    }
    
    return Text("Status: ${state.rawConnectionState}");
  },
);
```

6. **Start Measurement**:

Once connected (status is `Service Discovered`), you can start a measurement.

```dart
await smFitrus.startBFP(
  heightCm: 175.0,
  weightKg: 70.0,
  gender: FitrusGender.male, // or FitrusGender.female
  birth: '19950101', // Format: yyyyMMdd
);
```

## Data Model

The `FitrusModel` provides:
- `connectionState`: Current status (scanning, connected, disconnected, etc.)
- `progress`: Measurement progress (0-100%)
- `bodyFat`: The result object containing:
    - `fatPercentage`, `fatMass`, `muscleMass`
    - `bmi`, `bmr`
    - `waterPercentage`, `minerals`, `protein`
    - `icw` (Intracellular Water), `ecw` (Extracellular Water) 