import 'package:flutter_test/flutter_test.dart';
import 'package:sm_fitrus/fitrus_model.dart';
import 'package:sm_fitrus/sm_fitrus.dart';

void main() {
  // ============================================================
  // FitrusConnectionState.fromString() Tests
  // ============================================================
  group('FitrusConnectionState.fromString()', () {
    test('returns disconnected for "disconnected"', () {
      expect(
        FitrusConnectionState.fromString('disconnected'),
        FitrusConnectionState.disconnected,
      );
    });

    test('returns scanning for "scanning"', () {
      expect(
        FitrusConnectionState.fromString('scanning'),
        FitrusConnectionState.scanning,
      );
    });

    test('returns scanning for "searched"', () {
      expect(
        FitrusConnectionState.fromString('searched'),
        FitrusConnectionState.scanning,
      );
    });

    test('returns connecting for "connecting"', () {
      expect(
        FitrusConnectionState.fromString('connecting'),
        FitrusConnectionState.connecting,
      );
    });

    test('returns connected for "connected"', () {
      expect(
        FitrusConnectionState.fromString('connected'),
        FitrusConnectionState.connected,
      );
    });

    test('returns discoveringServices for "service discovered"', () {
      expect(
        FitrusConnectionState.fromString('service discovered'),
        FitrusConnectionState.discoveringServices,
      );
    });

    test('returns scanFailed for "scan failed"', () {
      expect(
        FitrusConnectionState.fromString('scan failed'),
        FitrusConnectionState.scanFailed,
      );
    });

    test('returns dataAvailable for states containing "data"', () {
      expect(
        FitrusConnectionState.fromString('data received'),
        FitrusConnectionState.dataAvailable,
      );
      expect(
        FitrusConnectionState.fromString('DATA AVAILABLE'),
        FitrusConnectionState.dataAvailable,
      );
    });

    test('is case-insensitive', () {
      expect(
        FitrusConnectionState.fromString('CONNECTED'),
        FitrusConnectionState.connected,
      );
      expect(
        FitrusConnectionState.fromString('DiScOnNeCtEd'),
        FitrusConnectionState.disconnected,
      );
    });

    test('returns unknown for null', () {
      expect(
        FitrusConnectionState.fromString(null),
        FitrusConnectionState.unknown,
      );
    });

    test('returns unknown for unrecognized states', () {
      expect(
        FitrusConnectionState.fromString('random_state'),
        FitrusConnectionState.unknown,
      );
      expect(
        FitrusConnectionState.fromString(''),
        FitrusConnectionState.unknown,
      );
    });
  });

  // ============================================================
  // FitrusModel.fromJson() Tests
  // ============================================================
  group('FitrusModel.fromJson()', () {
    test('parses all fields correctly', () {
      final json = {
        'connectState': 'connected',
        'hasData': true,
        'hasProgress': true,
        'progress': '75',
        'bmi': '24.5',
        'bmr': '1800',
        'fatPercentage': '20.5',
        'fatMass': '15.0',
        'muscleMass': '45.0',
        'waterPercentage': '55.0',
        'minerals': '3.5',
        'protein': '12.0',
        'calorie': '2000',
      };

      final model = FitrusModel.fromJson(json);

      expect(model.connectionState, FitrusConnectionState.connected);
      expect(model.rawConnectionState, 'connected');
      expect(model.hasData, true);
      expect(model.hasProgress, true);
      expect(model.progress, 75);
      expect(model.isConnected, true);
      expect(model.bodyFat, isNotNull);
      expect(model.bodyFat!.bmi, 24.5);
      expect(model.bodyFat!.bmr, 1800);
      expect(model.bodyFat!.fatPercentage, 20.5);
    });

    test('uses defaults for missing fields', () {
      final json = <String, dynamic>{};

      final model = FitrusModel.fromJson(json);

      expect(model.connectionState, FitrusConnectionState.unknown);
      expect(model.rawConnectionState, '');
      expect(model.hasData, false);
      expect(model.hasProgress, false);
      expect(model.progress, 0);
      expect(model.isConnected, false);
      expect(model.bodyFat, isNull);
    });

    test('isConnected is true for connected state', () {
      final json = {'connectState': 'connected'};
      final model = FitrusModel.fromJson(json);
      expect(model.isConnected, true);
    });

    test('isConnected is true for service discovered state', () {
      final json = {'connectState': 'service discovered'};
      final model = FitrusModel.fromJson(json);
      expect(model.isConnected, true);
    });

    test('isConnected is true for data available state', () {
      final json = {'connectState': 'data received'};
      final model = FitrusModel.fromJson(json);
      expect(model.isConnected, true);
    });

    test('isConnected is false for disconnected state', () {
      final json = {'connectState': 'disconnected'};
      final model = FitrusModel.fromJson(json);
      expect(model.isConnected, false);
    });

    test('parses progress from string', () {
      final json = {'progress': '50'};
      final model = FitrusModel.fromJson(json);
      expect(model.progress, 50);
    });

    test('parses progress from int', () {
      final json = {'progress': 50};
      final model = FitrusModel.fromJson(json);
      expect(model.progress, 50);
    });

    test('defaults progress to 0 for invalid value', () {
      final json = {'progress': 'invalid'};
      final model = FitrusModel.fromJson(json);
      expect(model.progress, 0);
    });

    test('bodyFat is null when hasData is false', () {
      final json = {
        'hasData': false,
        'bmi': '24.5',
        'bmr': '1800',
      };
      final model = FitrusModel.fromJson(json);
      expect(model.bodyFat, isNull);
    });

    test('bodyFat is populated when hasData is true', () {
      final json = {
        'hasData': true,
        'bmi': '24.5',
        'bmr': '1800',
      };
      final model = FitrusModel.fromJson(json);
      expect(model.bodyFat, isNotNull);
      expect(model.bodyFat!.bmi, 24.5);
      expect(model.bodyFat!.bmr, 1800);
    });
  });

  // ============================================================
  // BodyFat.fromJson() Tests
  // ============================================================
  group('BodyFat.fromJson()', () {
    test('parses all fields correctly', () {
      final json = {
        'bmi': '24.5',
        'bmr': '1800',
        'waterPercentage': '55.0',
        'fatMass': '15.0',
        'fatPercentage': '20.5',
        'muscleMass': '45.0',
        'minerals': '3.5',
        'protein': '12.0',
        'calorie': '2000',
      };

      final bodyFat = BodyFat.fromJson(json);

      expect(bodyFat.bmi, 24.5);
      expect(bodyFat.bmr, 1800);
      expect(bodyFat.waterPercentage, 55.0);
      expect(bodyFat.fatMass, 15.0);
      expect(bodyFat.fatPercentage, 20.5);
      expect(bodyFat.muscleMass, 45.0);
      expect(bodyFat.minerals, 3.5);
      expect(bodyFat.protein, 12.0);
      expect(bodyFat.calorie, 2000);
    });

    test('defaults to 0 for missing fields', () {
      final json = <String, dynamic>{};

      final bodyFat = BodyFat.fromJson(json);

      expect(bodyFat.bmi, 0);
      expect(bodyFat.bmr, 0);
      expect(bodyFat.waterPercentage, 0);
      expect(bodyFat.fatMass, 0);
      expect(bodyFat.fatPercentage, 0);
      expect(bodyFat.muscleMass, 0);
      expect(bodyFat.minerals, 0);
      expect(bodyFat.protein, 0);
      expect(bodyFat.calorie, 0);
    });

    test('parses numeric values from strings', () {
      final json = {
        'bmi': '25.5',
        'bmr': '1850.5',
      };

      final bodyFat = BodyFat.fromJson(json);

      expect(bodyFat.bmi, 25.5);
      expect(bodyFat.bmr, 1850.5);
    });

    test('parses numeric values from numbers', () {
      final json = {
        'bmi': 25.5,
        'bmr': 1850.5,
      };

      final bodyFat = BodyFat.fromJson(json);

      expect(bodyFat.bmi, 25.5);
      expect(bodyFat.bmr, 1850.5);
    });

    test('defaults to 0 for invalid values', () {
      final json = {
        'bmi': 'invalid',
        'bmr': null,
        'waterPercentage': 'not_a_number',
      };

      final bodyFat = BodyFat.fromJson(json);

      expect(bodyFat.bmi, 0);
      expect(bodyFat.bmr, 0);
      expect(bodyFat.waterPercentage, 0);
    });
  });

  // ============================================================
  // FitrusGender.toApiFormat() Tests
  // ============================================================
  group('FitrusGender.toApiFormat()', () {
    test('male returns "M"', () {
      expect(FitrusGender.male.toApiFormat(), 'M');
    });

    test('female returns "F"', () {
      expect(FitrusGender.female.toApiFormat(), 'F');
    });
  });

  // ============================================================
  // FitrusModel.toString() Tests
  // ============================================================
  group('FitrusModel.toString()', () {
    test('returns formatted string', () {
      const model = FitrusModel(
        connectionState: FitrusConnectionState.connected,
        progress: 50,
      );

      expect(model.toString(), contains('FitrusModel'));
      expect(model.toString(), contains('connected'));
      expect(model.toString(), contains('50'));
    });
  });

  // ============================================================
  // BodyFat.toString() Tests
  // ============================================================
  group('BodyFat.toString()', () {
    test('returns formatted string', () {
      const bodyFat = BodyFat(
        bmi: 24.5,
        bmr: 1800,
        fatPercentage: 20.5,
        fatMass: 15.0,
        muscleMass: 45.0,
      );

      final str = bodyFat.toString();
      expect(str, contains('BodyFat'));
      expect(str, contains('24.5'));
      expect(str, contains('1800'));
      expect(str, contains('20.5'));
    });
  });

  // ============================================================
  // FitrusModel const constructor Tests
  // ============================================================
  group('FitrusModel const constructor', () {
    test('default values are correct', () {
      const model = FitrusModel();

      expect(model.isConnected, false);
      expect(model.hasData, false);
      expect(model.hasProgress, false);
      expect(model.connectionState, FitrusConnectionState.disconnected);
      expect(model.rawConnectionState, '');
      expect(model.progress, 0);
      expect(model.bodyFat, isNull);
    });
  });

  // ============================================================
  // BodyFat const constructor Tests
  // ============================================================
  group('BodyFat const constructor', () {
    test('default values are correct', () {
      const bodyFat = BodyFat();

      expect(bodyFat.bmi, 0);
      expect(bodyFat.bmr, 0);
      expect(bodyFat.waterPercentage, 0);
      expect(bodyFat.fatMass, 0);
      expect(bodyFat.fatPercentage, 0);
      expect(bodyFat.muscleMass, 0);
      expect(bodyFat.minerals, 0);
      expect(bodyFat.protein, 0);
      expect(bodyFat.calorie, 0);
    });
  });
}
