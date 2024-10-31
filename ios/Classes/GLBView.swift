import Flutter
import UIKit
import SceneKit
import GLTFSceneKit

class GLBView: NSObject, FlutterPlatformView, UIGestureRecognizerDelegate {
    var sceneView = SCNView()
    var modelScene: SCNScene?
    let flutterChannel: FlutterMethodChannel
    
    init(
        frame: CGRect,
        viewIdentifier viewId: Int64,
        arguments args: Any?,
        binaryMessenger messenger: FlutterBinaryMessenger
    ) {
        self.sceneView = SCNView(frame: frame)
        self.flutterChannel = FlutterMethodChannel(name: "flutter_3d_viewer_channel_\(viewId)", binaryMessenger: messenger)
        super.init()
        self.sceneView.backgroundColor = .clear
        self.sceneView.allowsCameraControl = true
        self.sceneView.showsStatistics = false
        self.sceneView.autoenablesDefaultLighting = true
        self.flutterChannel.setMethodCallHandler(self.onAnchorMethodCalled)
    }
    
    func onAnchorMethodCalled(_ call :FlutterMethodCall, _ result: @escaping FlutterResult) {
        let arguments = call.arguments as? Dictionary<String, Any>
        
        switch call.method {
        case "showGLBFromURL":
            if let url = arguments!["url"] as? String {
                downloadGLBForURL(url: url)
            }
            result(true)
            break
        case "dispose":
            onDispose(result)
            break
        default:
            result(FlutterMethodNotImplemented)
            break
        }
    }
    
    func view() -> UIView {
        return self.sceneView
    }
    
    func onDispose(_ result:FlutterResult) {
        self.flutterChannel.setMethodCallHandler(nil)
        result(nil)
    }
    
    private func downloadGLBForURL(url: String) {
        print(url)
        self.downloadFile(urlString: url) { url in
            guard let modelFilePath = url else {
                print("Model file path is nil")
                return
            }
            self.flutterChannel.invokeMethod("onStartLoading", arguments: nil)
            DispatchQueue.global().async {
                do {
                    let sceneSource = GLTFSceneSource(url: modelFilePath)
                    let scene = try sceneSource.scene()
                    DispatchQueue.main.async {
                        self.flutterChannel.invokeMethod("onFinishLoading", arguments: nil)
                        self.sceneView.scene = scene
                    }
                } catch {
                    print(error)
                }
            }
        }
    }
    
    // Creates a node form a given glb model path
    func downloadFile(urlString: String, completion: @escaping (URL?) -> Void) {
        
        let downloadURL = URL(string: urlString)!
        let fileManager = FileManager.default;
        let paths = fileManager.urls(for: .libraryDirectory, in: .userDomainMask)
        let cacheDirectory = paths[0].appendingPathComponent("Caches/3d_files/")
        if !directoryIsExists(at: cacheDirectory.path) {
            do {
                try fileManager.createDirectory(at: cacheDirectory, withIntermediateDirectories: true)
            } catch {
                print(error)
                completion(nil)
                return
            }
        }
        
        let targetURL = cacheDirectory.appendingPathComponent(downloadURL.lastPathComponent)
        
        let path = targetURL.path
        let exsit = FileManager.default.fileExists(atPath: path)
        if exsit {
            completion(targetURL)
            return
        }
        
        let configuration = URLSessionConfiguration.default
        let session = URLSession(configuration: configuration)
        
        self.flutterChannel.invokeMethod("onStartDownload", arguments: nil)
        
        let task = session.downloadTask(with: downloadURL) { url, response, error in
            self.flutterChannel.invokeMethod("onFinishDownload", arguments: nil)
            // 检查是否有错误发生
            guard error == nil else {
                completion(nil)
                return
            }
            
            // 检查返回的数据是否有效
            guard let response = response as? HTTPURLResponse, (200...299).contains(response.statusCode) else {
                completion(nil)
                return
            }
            
            guard let fileURL = url else { return }
            
            do {
                try? FileManager.default.removeItem(at: targetURL)
                try FileManager.default.copyItem(at: fileURL, to: targetURL)
                completion(targetURL)
            } catch {
                completion(nil)
            }
        }
        task.resume()
    }
    
    func directoryIsExists(at path: String) -> Bool {
        var directoryExists = ObjCBool.init(false)
        let fileExists = FileManager.default.fileExists(atPath: path, isDirectory: &directoryExists)
        return fileExists && directoryExists.boolValue
    }
}
