import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_3d_viewer_method_channel.dart';

abstract class Flutter3dViewerPlatform extends PlatformInterface {
  /// Constructs a Flutter3dViewerPlatform.
  Flutter3dViewerPlatform() : super(token: _token);

  static final Object _token = Object();

  static Flutter3dViewerPlatform _instance = MethodChannelFlutter3dViewer();

  /// The default instance of [Flutter3dViewerPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutter3dViewer].
  static Flutter3dViewerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [Flutter3dViewerPlatform] when
  /// they register themselves.
  static set instance(Flutter3dViewerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
