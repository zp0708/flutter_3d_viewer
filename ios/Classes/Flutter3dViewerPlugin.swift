import Flutter
import UIKit

public class Flutter3dViewerPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_3d_viewer", binaryMessenger: registrar.messenger())
    let instance = Flutter3dViewerPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    let factory = GLBViewFactory(messenger: registrar.messenger())
    registrar.register(factory, withId: "flutter_3d_viewer")
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}
