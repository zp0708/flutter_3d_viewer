import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class Flutter3DViewWrap extends StatelessWidget {
  const Flutter3DViewWrap({
    super.key,
    required this.url,
    this.width = 0,
    this.height = 0,
  });

  final String url;

  // width 地图宽度， height地图高度
  final double width, height;

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = 'flutter_3d_viewer';
    // Pass parameters to the platform side.
    final Map<String, dynamic> creationParams = <String, dynamic>{'width': width, 'height': height, 'url': url};

    return SizedBox(width: width, height: height, child: _nativeMapView(viewType, creationParams));
  }

  Widget _nativeMapView(String viewType, Map<String, dynamic> creationParams) {
    if (Platform.isAndroid) {
      return AndroidView(
        viewType: viewType,
        layoutDirection: TextDirection.ltr,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    } else {
      return UiKitView(
        viewType: viewType,
        layoutDirection: TextDirection.ltr,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    }
  }
}
