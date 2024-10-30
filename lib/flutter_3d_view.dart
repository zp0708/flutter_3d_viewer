import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_3d_viewer/native_channel_manager.dart';

class Flutter3DViewWrap extends StatefulWidget {
  const Flutter3DViewWrap({
    super.key,
    required this.url,
    this.indicatorColor = Colors.white,
    this.width = 0,
    this.height = 0,
  });

  final String url;

  final Color indicatorColor;

  // width 地图宽度， height地图高度
  final double width, height;

  @override
  State<Flutter3DViewWrap> createState() => _Flutter3DViewWrapState();
}

class _Flutter3DViewWrapState extends State<Flutter3DViewWrap> {
  late NativeChannelManager _channelManager;

  ViewerStatus loadingStatus = ViewerStatus.startDownload;

  @override
  void initState() {
    super.initState();
  }

  void onPlatformViewCreated(int id) {
    print("platform view created! id: $id");
    _channelManager = NativeChannelManager(id, debug: true);
    _channelManager.onStatusChanged = (ViewerStatus status) {
      print('flutter channel status change $status');
      setState(() {
        loadingStatus = status;
      });
    };
    _channelManager.showGLBFromURL(widget.url);
  }

  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = 'flutter_3d_viewer';
    // Pass parameters to the platform side.
    final Map<String, dynamic> creationParams = <String, dynamic>{
      'width': widget.width,
      'height': widget.height,
      'url': widget.url,
    };

    return SizedBox(
      width: widget.width,
      height: widget.height,
      child: Stack(
        children: [
          if (loadingStatus != ViewerStatus.finishLoading)
            Center(
              child: CircularProgressIndicator(
                color: widget.indicatorColor,
              ),
            ),
          Positioned.fill(
            child: _nativeMapView(
              viewType,
              creationParams,
            ),
          ),
        ],
      ),
    );
  }

  Widget _nativeMapView(String viewType, Map<String, dynamic> creationParams) {
    if (Platform.isAndroid) {
      return AndroidView(
        viewType: viewType,
        layoutDirection: TextDirection.ltr,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
        onPlatformViewCreated: onPlatformViewCreated,
      );
    } else {
      return UiKitView(
        viewType: viewType,
        layoutDirection: TextDirection.ltr,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
        onPlatformViewCreated: onPlatformViewCreated,
      );
    }
  }
}
