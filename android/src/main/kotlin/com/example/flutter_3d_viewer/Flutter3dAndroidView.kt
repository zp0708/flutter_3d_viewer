package com.example.flutter_3d_viewer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.Log
import android.view.Choreographer
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.filament.Engine
import com.google.android.filament.Fence
import com.google.android.filament.Skybox
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.utils.*
import com.google.android.filament.utils.RemoteServer.ReceivedMessage
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

@SuppressLint("ClickableViewAccessibility")
internal class Flutter3dAndroidView(
        context: Context,
        messenger: BinaryMessenger,
        id: Int,
        creationParams: Map<String?, Any?>?
) : PlatformView {
    companion object {
        init {
            Utils.init()
        }
    }

    // constants
    private val TAG: String = Flutter3dAndroidView::class.java.name

    private val viewContext: Context
    // Platform channel
    private val flutterChannel: MethodChannel = MethodChannel(messenger, "flutter_3d_viewer_channel_$id")

    // UI variables
    private var surfaceView: SurfaceView
    private var choreographer: Choreographer
    private var modelViewer: ModelViewer
    private var remoteServer: RemoteServer? = null
    private var latestDownload: String? = null
    private val automation = AutomationEngine()
    private var loadStartTime = 0L
    private var loadStartFence: Fence? = null
    private val viewerContent = AutomationEngine.ViewerContent()
    private val frameScheduler = FrameCallback()
    private val downloadManager = FileDownloadManager()

    init {
        Log.d(TAG, "Initializing Flutter3dAndroidView")
        viewContext = context
        surfaceView = SurfaceView(context)
        surfaceView.layoutParams = FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        surfaceView.setBackgroundColor(Color.TRANSPARENT)
        surfaceView.visibility = View.GONE
        choreographer = Choreographer.getInstance()

        modelViewer = ModelViewer(surfaceView)
        viewerContent.view = modelViewer.view
        modelViewer.cameraFocalLength = 48.0f
        viewerContent.sunlight = modelViewer.light
        viewerContent.lightManager = modelViewer.engine.lightManager
        viewerContent.scene = modelViewer.scene
        modelViewer.renderer.clearOptions = modelViewer.renderer.clearOptions.apply {
            clear = true
        }
        surfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)
        viewerContent.renderer = modelViewer.renderer

        surfaceView.setOnTouchListener { _, event ->
            modelViewer.onTouchEvent(event)
            true
        }

        createIndirectLight()

        val view = modelViewer.view

        view.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT
        modelViewer.scene.skybox = null

        view.renderQuality = view.renderQuality.apply {
            hdrColorBuffer = com.google.android.filament.View.QualityLevel.MEDIUM
        }

        view.dynamicResolutionOptions = view.dynamicResolutionOptions.apply {
            enabled = true
            quality = com.google.android.filament.View.QualityLevel.MEDIUM
        }

        view.multiSampleAntiAliasingOptions = view.multiSampleAntiAliasingOptions.apply {
            enabled = true
        }

        view.antiAliasing = com.google.android.filament.View.AntiAliasing.FXAA

        view.ambientOcclusionOptions = view.ambientOcclusionOptions.apply {
            enabled = true
        }

        view.bloomOptions = view.bloomOptions.apply {
            enabled = true
        }

        setMethodHandler()
    }

    override fun getView(): View {
        return surfaceView
    }

    private fun downloadGLBFile(url: String) {
        val file = downloadManager.getGLBFile(viewContext, url)
        if (file.exists()) {
            flutterChannel.invokeMethod("onStartLoading", null)
            GlobalScope.launch {
                loadGltf(file)
            }
        } else {
            flutterChannel.invokeMethod("onStartDownload", null)
            downloadManager.downloadFile(viewContext, url, object : DownloadCallback {
                override fun onProgressUpdate(progress: Int) {
                    Log.d("Download", "Progress: $progress%")
                    // 这里可以更新 UI 中的进度条
                }

                override fun onDownloadSuccess(filePath: String) {
                    Log.d("Download", "File downloaded successfully: $filePath")
                    // 下载成功，可以在这里通知用户
                    flutterChannel.invokeMethod("onFinishDownload", null)
                    flutterChannel.invokeMethod("onStartLoading", null)
                    GlobalScope.launch {
                        loadGltf(file)
                    }
                }

                override fun onDownloadFailed(errorMessage: String) {
                    Log.e("Download", "Download failed: $errorMessage")
                    // 下载失败，可以在这里提示错误
                }
            })
        }
    }

    private suspend fun loadGltf(file: File) {
        withContext(Dispatchers.IO) {
            val size = file.length().toInt()
            val bytes = ByteArray(size)
            try {
                val buf = BufferedInputStream(FileInputStream(file))
                buf.read(bytes, 0, bytes.size)
                buf.close()
                withContext(Dispatchers.Main) {
                    flutterChannel.invokeMethod("onFinishLoading", null)
                    createDefaultRenderables(bytes)
                }
            } catch (e: FileNotFoundException) {
                Log.i(TAG, e.toString())
                e.printStackTrace()
            } catch (e: IOException) {
                Log.i(TAG, e.toString())
                e.printStackTrace()
            }
        }
    }

    private fun createDefaultRenderables(bytes: ByteArray) {
        surfaceView.visibility = View.VISIBLE
        try {
            modelViewer.loadModelGltfAsync(ByteBuffer.wrap(bytes)) { uri ->
                readCompressedAsset("models/$uri")
            }
            updateRootTransform()
            choreographer.postFrameCallback(frameScheduler)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    private fun createIndirectLight() {
        val engine = modelViewer.engine
        val scene = modelViewer.scene
        val ibl = "default_env"
        readCompressedAsset("env/${ibl}_ibl.ktx").let {
            scene.indirectLight = KTX1Loader.createIndirectLight(engine, it)
            scene.indirectLight!!.intensity = 30_000.0f
            viewerContent.indirectLight = modelViewer.scene.indirectLight
        }
//        modelViewer.scene.skybox = Skybox.Builder().color(.62f, 0.64f, 0.78f, 1f).build(modelViewer.engine)
//        readCompressedAsset("env/${ibl}_skybox.ktx").let {
//            scene.skybox = KTX1Loader.createSkybox(engine, it)
//        }
    }

    private fun readCompressedAsset(assetName: String): ByteBuffer {
        val input = viewContext.assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    private suspend fun loadGlb(message: RemoteServer.ReceivedMessage) {
        withContext(Dispatchers.Main) {
            modelViewer.destroyModel()
            modelViewer.loadModelGlb(message.buffer)
            updateRootTransform()
            loadStartTime = System.nanoTime()
            loadStartFence = modelViewer.engine.createFence()
        }
    }

    fun loadModelData(message: ReceivedMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            loadGlb(message)
        }
    }

    fun loadSettings(message: ReceivedMessage) {
        val json = StandardCharsets.UTF_8.decode(message.buffer).toString()
        viewerContent.assetLights = modelViewer.asset?.lightEntities
        automation.applySettings(modelViewer.engine, json, viewerContent)
        modelViewer.view.colorGrading = automation.getColorGrading(modelViewer.engine)
        modelViewer.cameraFocalLength = automation.viewerOptions.cameraFocalLength
        modelViewer.cameraFar = automation.viewerOptions.cameraFar
        modelViewer.cameraNear = automation.viewerOptions.cameraNear
        updateRootTransform()
    }

    fun updateRootTransform() {
        if (automation.viewerOptions.autoScaleEnabled) {
            modelViewer.transformToUnitCube()
        } else {
            modelViewer.clearRootTransform()
        }
    }

    inner class FrameCallback : Choreographer.FrameCallback {
        private val startTime = System.nanoTime()
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)

            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    val elapsedTimeSeconds = (frameTimeNanos - startTime).toDouble() / 1_000_000_000
                    applyAnimation(0, elapsedTimeSeconds.toFloat())
                }
                updateBoneMatrices()
            }

            modelViewer.render(frameTimeNanos)

            // Check if a new download is in progress. If so, let the user know with toast.
            val currentDownload = remoteServer?.peekIncomingLabel()
            if (RemoteServer.isBinary(currentDownload) && currentDownload != latestDownload) {
                latestDownload = currentDownload
                Log.i(TAG, "Downloading $currentDownload")
            }

            // Check if a new message has been fully received from the client.
            val message = remoteServer?.acquireReceivedMessage()
            if (message != null) {
                if (message.label == latestDownload) {
                    latestDownload = null
                }
                if (RemoteServer.isJson(message.label)) {
                    loadSettings(message)
                } else {
                    loadModelData(message)
                }
            }
        }
    }

    private fun setMethodHandler() {
        val onSessionMethodCall =
            MethodChannel.MethodCallHandler { call, result ->
                Log.d(TAG, "AndroidARView onsessionmethodcall reveived a call!")
                when (call.method) {
                    "showGLBFromURL" -> {
                        val url = call.argument<String>("url")
                        if (url != null) {
                            downloadGLBFile(url)
                        }
                    }
                    "dispose" -> {
                        onResume()
                    }
                    else -> {}
                }
            }
        flutterChannel.setMethodCallHandler(onSessionMethodCall)
    }

    fun onResume() {

    }

    override fun dispose() {
        choreographer.removeFrameCallback(frameScheduler)
    }
}


