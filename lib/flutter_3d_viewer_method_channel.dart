import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_3d_viewer_platform_interface.dart';

/// An implementation of [Flutter3dViewerPlatform] that uses method channels.
class MethodChannelFlutter3dViewer extends Flutter3dViewerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_3d_viewer');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
