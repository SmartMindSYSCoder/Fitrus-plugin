import 'package:flutter_test/flutter_test.dart';
import 'package:sm_fitrus/sm_fitrus.dart';
import 'package:sm_fitrus/sm_fitrus_platform_interface.dart';
import 'package:sm_fitrus/sm_fitrus_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockSmFitrusPlatform
    with MockPlatformInterfaceMixin
    implements SmFitrusPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final SmFitrusPlatform initialPlatform = SmFitrusPlatform.instance;

  test('$MethodChannelSmFitrus is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelSmFitrus>());
  });

  test('getPlatformVersion', () async {
    SmFitrus smFitrusPlugin = SmFitrus();
    MockSmFitrusPlatform fakePlatform = MockSmFitrusPlatform();
    SmFitrusPlatform.instance = fakePlatform;

    expect(await smFitrusPlugin.getPlatformVersion(), '42');
  });
}
