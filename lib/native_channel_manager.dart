import 'package:flutter/services.dart';

enum ViewerStatus { startDownload, finishDownload, startLoading, finishLoading }

// Type definitions to enforce a consistent use of the API
typedef NativeChannelHandler = void Function(ViewerStatus);

/// Handles all anchor-related functionality of an [ARView], including configuration and usage of collaborative sessions
class NativeChannelManager {
  /// Platform channel used for communication from and to [ARAnchorManager]
  late MethodChannel _channel;

  /// Debugging status flag. If true, all platform calls are printed. Defaults to false.
  final bool debug;

  /// Callback that is triggered once an anchor has successfully been uploaded to the google cloud anchor API
  NativeChannelHandler? onStatusChanged;

  NativeChannelManager(int id, {this.debug = false}) {
    _channel = MethodChannel('flutter_3d_viewer_channel_$id');
    _channel.setMethodCallHandler(_platformCallHandler);
    if (debug) {
      print("ARAnchorManager initialized");
    }
  }

  Future<dynamic> _platformCallHandler(MethodCall call) async {
    if (debug) {
      print('_platformCallHandler call ${call.method} ${call.arguments}');
    }
    try {
      switch (call.method) {
        case 'onError':
          print(call.arguments);
          break;
        case 'onStartLoading':
          onStatusChanged!(ViewerStatus.startLoading);
          break;
        case "onFinishLoading":
          onStatusChanged!(ViewerStatus.finishLoading);
          break;
        case "onStartDownload":
          onStatusChanged!(ViewerStatus.startDownload);
          break;
        case "onFinishDownload":
          onStatusChanged!(ViewerStatus.finishDownload);
          break;
        default:
          if (debug) {
            print('Unimplemented method ${call.method} ');
          }
      }
    } catch (e) {
      print('Error caught: $e');
    }
    return Future.value();
  }

  /// Add given anchor to the underlying AR scene
  Future<bool?> showGLBFromURL(String url) async {
    try {
      return await _channel.invokeMethod<bool>('showGLBFromURL', {'url': url});
    } on PlatformException catch (e) {
      return false;
    }
  }

  /// Dispose the AR view on the platforms to pause the scenes and disconnect the platform handlers.
  /// You should call this before removing the AR view to prevent out of memory erros
  dispose() async {
    try {
      await _channel.invokeMethod<void>("dispose");
    } catch (e) {
      print(e);
    }
  }
}
