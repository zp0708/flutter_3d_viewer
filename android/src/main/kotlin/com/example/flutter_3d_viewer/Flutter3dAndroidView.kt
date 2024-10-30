package com.example.flutter_3d_viewer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.FloatBuffer
import java.util.concurrent.CompletableFuture
import androidx.lifecycle.lifecycleScope
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch

import android.R
import android.view.Gravity

import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

internal class Flutter3dAndroidView(
        context: Context,
        messenger: BinaryMessenger,
        id: Int,
        creationParams: Map<String?, Any?>?
) : PlatformView {
    // constants
    private val TAG: String = Flutter3dAndroidView::class.java.name
    // Lifecycle variables
    private var mUserRequestedInstall = true
    lateinit var activityLifecycleCallbacks: Application.ActivityLifecycleCallbacks
    private val viewContext: Context
    // Platform channels
    private val sessionManagerChannel: MethodChannel = MethodChannel(messenger, "arsession_$id")

    // UI variables
    private lateinit var arSceneView: View
    // private var modelBuilder = ArModelBuilder()
    // Cloud anchor handler
    private lateinit var sceneView: SceneView
    private lateinit var loadingView: View


    // Method channel handlers
    private val onSessionMethodCall =
        MethodChannel.MethodCallHandler { call, result ->
            Log.d(TAG, "AndroidARView onsessionmethodcall reveived a call!")
            when (call.method) {
                "init" -> {

                }

                "dispose" -> {
                    dispose()
                }

                else -> {}
            }
        }

    override fun getView(): View {
        return arSceneView
    }

    override fun dispose() {
        // Destroy AR session
        Log.d(TAG, "dispose called")
        try {
            onPause()
            onDestroy()
//            ArSceneView.destroyAllResources()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        Log.d(TAG, "Initializing AndroidARView")
        viewContext = context
//        arSceneView = ArSceneView(context)
        sceneView = SceneView(context)

//            val hdrFile = "environments/studio_small_09_2k.hdr"
//            sceneView.environmentLoader.loadHDREnvironment(hdrFile).apply {
//                sceneView.indirectLight = this?.indirectLight
//                sceneView.skybox = this?.skybox
//            }
            sceneView.cameraNode.apply {
                position = Position(z = 4.0f)
            }
            val modelFile = "models/Astronaut.glb"
            val modelInstance = sceneView.modelLoader.createModelInstance(modelFile)

            val modelNode = ModelNode(
                modelInstance = modelInstance,
                scaleToUnits = 2.0f,
            )
            modelNode.scale = Scale(0.05f)
            sceneView.addChildNode(modelNode)
            loadingView.isGone = true

        setupLifeCycle(context)
        sessionManagerChannel.setMethodCallHandler(onSessionMethodCall)
    }

    private fun setupLifeCycle(context: Context) {
        activityLifecycleCallbacks =
                object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityCreated(
                            activity: Activity,
                            savedInstanceState: Bundle?
                    ) {
                        Log.d(TAG, "onActivityCreated")
                    }

                    override fun onActivityStarted(activity: Activity) {
                        Log.d(TAG, "onActivityStarted")
                    }

                    override fun onActivityResumed(activity: Activity) {
                        Log.d(TAG, "onActivityResumed")
                        onResume()
                    }

                    override fun onActivityPaused(activity: Activity) {
                        Log.d(TAG, "onActivityPaused")
                        onPause()
                    }

                    override fun onActivityStopped(activity: Activity) {
                        Log.d(TAG, "onActivityStopped")
                        // onStopped()
                        onPause()
                    }

                    override fun onActivitySaveInstanceState(
                            activity: Activity,
                            outState: Bundle
                    ) {}

                    override fun onActivityDestroyed(activity: Activity) {
                        Log.d(TAG, "onActivityDestroyed")
//                        onPause()
//                        onDestroy()
                    }
                }

//        activity.application.registerActivityLifecycleCallbacks(this.activityLifecycleCallbacks)
    }

    fun onResume() {
        // Create session if there is none
    }

    fun onPause() {

    }

    fun onDestroy() {

    }

    private fun initializeARView(call: MethodCall, result: MethodChannel.Result) {

    }
}

