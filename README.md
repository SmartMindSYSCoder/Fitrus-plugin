# sm_fitrus

[![Pub Version](https://img.shields.io/pub/v/sm_fitrus)](https://pub.dev/packages/sm_fitrus)
[![Flutter Platform](https://img.shields.io/badge/Platform-Flutter-02569B?logo=flutter)](https://flutter.dev)

A professional Flutter plugin for integrating with the **Fitrus** Body Composition device.

This plugin provides a seamless way to connect to Fitrus devices via BLE, measure various body metrics, and receive real-time data streams.

---

## 🚀 Features

- **BLE Connectivity**: Reliable connection management (scan, connect, disconnect).
- **Comprehensive Analysis**: Measure BFP, Muscle Mass, BMR, BMI, Water %, Minerals, and more.
- **Smart Calculations**: Auto-calculates derived metrics (BMI, Water %) if missing from the API.
- **Robust Persistence**: Persists measurement results even during temporary connection drops.
- **Real-time Streams**: Live updates for connection state and measurement progress.

---

## 📋 Prerequisites

### Android

This plugin requires specific permissions for Bluetooth and Location (needed for scanning on older Android versions).

Add the following to your `android/app/src/main/AndroidManifest.xml`:

```xml
<!-- Internet & Network (Required for API access) -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Bluetooth Permissions -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<!-- Location (Required for BLE scanning on Android 11 and below) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- Android 12+ (Target SDK 31+) -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

> [!IMPORTANT]
> - **Internet Connection**: Required for the Fitrus API to calculate body composition.
> - **GPS**: Must be enabled on Android for BLE scanning to function.

---

## 🛠 Getting Started

### 1. Installation

Add `sm_fitrus` to your `pubspec.yaml`:

```yaml
dependencies:
  sm_fitrus:
    git: https://github.com/SmartMindSYSCoder/Fitrus-plugin.git
```

### 2. Implementation Logic

> [!TIP]
> **Best Practice**: Always set up your event listener **before** initializing the plugin. This ensures you capture all initial connection states and don't miss any events.

```dart
final smFitrus = SmFitrus();

// 1. First, set up the listener to handle streams
smFitrus.getEvents().listen((data) {
  print("Connection State: ${data.connectionState}");
  if (data.hasData) {
    print("Body Fat: ${data.bodyFat?.fatPercentage}%");
  }
});

// 2. Request necessary runtime permissions
await smFitrus.getPermissions();

// 3. Initialize with your API Key
await smFitrus.init(
  apiKey: 'YOUR_API_KEY_HERE'
);
```

### 3. Usage Examples

You can consume the data in two ways:

#### Option A: Reactive UI (StreamBuilder)

Perfect for building the UI directly from the data stream.

```dart
StreamBuilder<FitrusModel>(
  stream: smFitrus.getEvents(),
  builder: (context, snapshot) {
    if (!snapshot.hasData) return Text("Waiting for connection...");
    
    final data = snapshot.data!;
    
    // 1. Check Connection State
    if (data.connectionState == FitrusConnectionState.connected) {
      return Text("Connected! Ready to measure.");
    }
    
    // 2. Display Results
    if (data.hasData && data.bodyFat != null) {
      final bodyFat = data.bodyFat!;
      return Column(children: [
        _buildMetric("Body Fat", "${bodyFat.fatPercentage}%"),
        _buildMetric("Muscle Mass", "${bodyFat.muscleMass} kg"),
        _buildMetric("BMI", "${bodyFat.bmi}"),
        _buildMetric("Water", "${bodyFat.waterPercentage}%"),
        _buildMetric("Minerals", "${bodyFat.minerals} kg"),
      ]);
    }
    
    // 3. Fallback Status
    return Text("Status: ${data.rawConnectionState}");
  },
);
```

#### Option B: Business Logic (Event Listener)

Useful for state management (GetX, Bloc, Provider) or non-UI logic.

```dart
smFitrus.getEvents().listen((data) {
  // Handle connection changes
  if (data.connectionState == FitrusConnectionState.disconnected) {
    print("Device disconnected");
  }

  // Handle measurement results
  if (data.hasData && data.bodyFat != null) {
    print("New Measurement Received:");
    print("Body Fat: ${data.bodyFat?.fatPercentage}%");
    print("Skeletal Muscle: ${data.bodyFat?.muscleMass}kg");
    
    // Save to database or update state controller
    // myController.updateBodyFat(data.bodyFat);
  }
});
```

### 4. Starting a Measurement

Once the connection status is **Service Discovered**, you can trigger a measurement.

```dart
await smFitrus.startBFP(
  heightCm: 175.0,
  weightKg: 70.0,
  gender: FitrusGender.male, // or FitrusGender.female
  birth: '19950101',         // Format: yyyyMMdd
);
```

---

## 📊 Data Model

The `FitrusModel` object contains all necessary information:

- **`connectionState`**: (Enum) Current status (`scanning`, `connected`, `disconnected`, etc.)
- **`progress`**: (Int) Measurement progress (0-100%).
- **`bodyFat`**: (Object) The comprehensive result data:
    - `fatPercentage (%)`
    - `fatMass (kg)`
    - `muscleMass (kg)`
    - `bmi`
    - `bmr (kcal)`
    - `waterPercentage (%)`
    - `minerals (kg)`
    - `protein (kg)`
    - `icw` (Intracellular Water)
    - `ecw` (Extracellular Water) 