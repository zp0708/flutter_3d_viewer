
import 'flutter_3d_viewer_platform_interface.dart';

class Flutter3dViewer {
  Future<String?> getPlatformVersion() {
    return Flutter3dViewerPlatform.instance.getPlatformVersion();
  }
}
