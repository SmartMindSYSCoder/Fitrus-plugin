enum FitrusConnectionState {
  disconnected,
  scanning,
  connecting,
  connected,
  discoveringServices,
  dataAvailable,
  scanFailed,
  unknown;

  static FitrusConnectionState fromString(String? state) {
    if (state == null) return FitrusConnectionState.unknown;
    switch (state.toLowerCase()) {
      case 'disconnected':
        return FitrusConnectionState.disconnected;
      case 'searched':
      case 'scanning':
        return FitrusConnectionState.scanning;
      case 'connecting':
        return FitrusConnectionState.connecting;
      case 'connected':
        return FitrusConnectionState.connected;
      case 'service discovered':
        return FitrusConnectionState.discoveringServices;
      case 'scan failed':
        return FitrusConnectionState.scanFailed;
      default:
        // Checking for partial matches as per legacy logic, though ideally we want exact matches
        if (state.toLowerCase().contains("data")) {
          return FitrusConnectionState.dataAvailable;
        }
        return FitrusConnectionState.unknown;
    }
  }
}

class FitrusModel {
  final bool isConnected;
  final bool hasData;
  final bool hasProgress;
  final FitrusConnectionState connectionState;
  final String rawConnectionState; // Keep raw string just in case
  final int progress;
  final BodyFat? bodyFat;

  const FitrusModel({
    this.isConnected = false,
    this.hasData = false,
    this.hasProgress = false,
    this.connectionState = FitrusConnectionState.disconnected,
    this.rawConnectionState = '',
    this.progress = 0,
    this.bodyFat,
  });

  factory FitrusModel.fromJson(Map<String, dynamic> json) {
    final rawState = json['connectState'] as String? ?? '';
    final state = FitrusConnectionState.fromString(rawState);

    // Legacy logic for 'isConnected' was a bit complex, simplifying based on state usually.
    // However, maintaining similar logic to original for compatibility + robustness:
    /*
      original: data['connectState'] !=null  && data['connectState'].toString().toLowerCase().contains("data")  || ['Connected','Service Discovered'].contains(data['connectState'].toString())
    */
    final bool derivedIsConnected = state == FitrusConnectionState.connected ||
        state == FitrusConnectionState.discoveringServices ||
        state == FitrusConnectionState.dataAvailable ||
        rawState.toLowerCase().contains('data');

    return FitrusModel(
      isConnected: derivedIsConnected,
      hasData: json['hasData'] ?? false,
      hasProgress: json['hasProgress'] ?? false,
      connectionState: state,
      rawConnectionState: rawState,
      progress:
          (num.tryParse(json['progress']?.toString() ?? '0') ?? 0).toInt(),
      bodyFat: json['hasData'] == true ? BodyFat.fromJson(json) : null,
    );
  }

  @override
  String toString() {
    return 'FitrusModel(state: $connectionState, progress: $progress, bodyFat: $bodyFat)';
  }
}

class BodyFat {
  final double bmi;
  final double bmr;
  final double waterPercentage;
  final double fatMass;
  final double fatPercentage;
  final double muscleMass;
  final double minerals;
  final double protein;
  final double calorie;

  const BodyFat({
    this.bmi = 0,
    this.bmr = 0,
    this.fatMass = 0,
    this.fatPercentage = 0,
    this.muscleMass = 0,
    this.waterPercentage = 0,
    this.calorie = 0,
    this.minerals = 0,
    this.protein = 0,
  });

  factory BodyFat.fromJson(Map<String, dynamic> json) {
    return BodyFat(
      bmi: double.tryParse(json['bmi']?.toString() ?? '0') ?? 0.0,
      bmr: double.tryParse(json['bmr']?.toString() ?? '0') ?? 0.0,
      waterPercentage:
          double.tryParse(json['waterPercentage']?.toString() ?? '0') ?? 0.0,
      fatMass: double.tryParse(json['fatMass']?.toString() ?? '0') ?? 0.0,
      fatPercentage:
          double.tryParse(json['fatPercentage']?.toString() ?? '0') ?? 0.0,
      muscleMass: double.tryParse(json['muscleMass']?.toString() ?? '0') ?? 0.0,
      protein: double.tryParse(json['protein']?.toString() ?? '0') ?? 0.0,
      calorie: double.tryParse(json['calorie']?.toString() ?? '0') ?? 0.0,
      minerals: double.tryParse(json['minerals']?.toString() ?? '0') ?? 0.0,
    );
  }

  @override
  String toString() {
    return "BodyFat(BMI:$bmi, BMR:$bmr, Fat%:$fatPercentage, FatMass:$fatMass, Muscle:$muscleMass)";
  }
}
