import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_3d_viewer/flutter_3d_viewer.dart';
import 'package:flutter_3d_viewer/flutter_3d_viewer_platform_interface.dart';
import 'package:flutter_3d_viewer/flutter_3d_viewer_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutter3dViewerPlatform
    with MockPlatformInterfaceMixin
    implements Flutter3dViewerPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final Flutter3dViewerPlatform initialPlatform = Flutter3dViewerPlatform.instance;

  test('$MethodChannelFlutter3dViewer is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutter3dViewer>());
  });

  test('getPlatformVersion', () async {
    Flutter3dViewer flutter3dViewerPlugin = Flutter3dViewer();
    MockFlutter3dViewerPlatform fakePlatform = MockFlutter3dViewerPlatform();
    Flutter3dViewerPlatform.instance = fakePlatform;

    expect(await flutter3dViewerPlugin.getPlatformVersion(), '42');
  });
}
