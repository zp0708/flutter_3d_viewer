import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_3d_viewer/flutter_3d_view.dart';
import 'package:flutter_3d_viewer/flutter_3d_viewer.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _flutter3dViewerPlugin = Flutter3dViewer();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await _flutter3dViewerPlugin.getPlatformVersion() ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: const Center(
          child: Column(
            children: [
              Flutter3DViewWrap(
                height: 300,
                width: 300,
                url:
                    'https://pb-wolf-temp.materia-app.xyz/api/files/6u8j3gk3siljtfg/txm2o5eh8ygku97/6717a913507f09d109095045769a795894bc02d554_3_d_avatar_tuThDMZ460.glb?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3OTI1NTEyODAsImlkIjoiYTQ1b3Qwbm5xNnV0eDd0IiwidHlwZSI6ImFkbWluIn0.KAyHwgq-Bzj2i5rnYo-ucuEaW5Jxpz2Wa2PJWtCULNg',
              ),
              Flutter3DViewWrap(
                width: 300,
                height: 300,
                url:
                    'https://pb-wolf-temp.materia-app.xyz/api/files/6u8j3gk3siljtfg/0k4s5dlb7kofxwa/67185b7ddf063629934daa4cc284688ec3bfbc8494_3_d_avatar_b4umycgCap.glb?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3OTI1NTEyODAsImlkIjoiYTQ1b3Qwbm5xNnV0eDd0IiwidHlwZSI6ImFkbWluIn0.KAyHwgq-Bzj2i5rnYo-ucuEaW5Jxpz2Wa2PJWtCULNg',
              ),
            ],
          ),
        ),
      ),
    );
  }
}
