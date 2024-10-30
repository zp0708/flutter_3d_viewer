package com.example.flutter_3d_viewer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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
    // private var modelBuilder = ArModelBuilder()
    // Cloud anchor handler
    private lateinit var sceneView: SceneView
//    private lateinit var loadingView: View


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
        return sceneView
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

        sceneView = SceneView(context)
        sceneView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)

        setupLifeCycle(context)
        sessionManagerChannel.setMethodCallHandler(onSessionMethodCall)
        downloadFile()
    }

    private fun downloadFile() {
        val url = "https://pb-wolf-temp.materia-app.xyz/api/files/6u8j3gk3siljtfg/gow9zpeolj6x1pu/67203d816f308aaf75f050435e9890aa6dfae50f50_3_d_avatar_moOvkDDCM2.glb?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3OTI1NTEyODAsImlkIjoiYTQ1b3Qwbm5xNnV0eDd0IiwidHlwZSI6ImFkbWluIn0.KAyHwgq-Bzj2i5rnYo-ucuEaW5Jxpz2Wa2PJWtCULNg"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Download failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "Download failed: Response code ${response.code}")
                    return
                }

                val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "MyAppDownloads")
                if (!directory.exists()) {
                    directory.mkdirs()
                }

                val file = File(directory, "downloaded_file.glb")

                displayGlb(file)

//                val responseBody = response.body
//                val bytes = responseBody?.bytes()
//
//                try {
//                    FileOutputStream(file).use { fos ->
//                        fos.write(bytes)
//                    }
//                    Log.i(TAG, "Download completed and saved to ${file.absolutePath}")
//                } catch (e: IOException) {
//                    Log.e(TAG, "Error writing file: ${e.message}")
//                }
            }
        })
    }

    private fun displayGlb(file: File) {
        sceneView.cameraNode.apply {
            position = Position(z = 4.0f)
        }
        val modelFile = "models/avatar-v1.glb"
        val modelInstance = sceneView.modelLoader.createModelInstance(modelFile)

        val modelNode = ModelNode(
            modelInstance = modelInstance,
            scaleToUnits = 2.0f,
            centerOrigin = Position(0.0F, 0.0F, 0.0F)
        )
//        modelNode.scale = Scale(0.05f)
        sceneView.addChildNode(modelNode)
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
                ) {
                }

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


