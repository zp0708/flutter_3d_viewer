package io.carius.lars.ar_flutter_plugin

import android.app.Activity
import android.content.Context
import com.example.flutter_3d_viewer.Flutter3dAndroidView
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class Flutter3dViewFactory(val messenger: BinaryMessenger) :
        PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val creationParams = args as Map<String?, Any?>?
        return Flutter3dAndroidView(context, messenger, viewId, creationParams)
    }
}
