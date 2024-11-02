package com.example.flutter_3d_viewer
import android.content.Context
import android.os.Environment
import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

interface DownloadCallback {
    fun onProgressUpdate(progress: Int)     // 下载进度更新
    fun onDownloadSuccess(filePath: String) // 下载成功
    fun onDownloadFailed(errorMessage: String) // 下载失败
}

class FileDownloadManager {
    fun downloadFile(
        context: Context,
        fileUrl: String,
        callback: DownloadCallback
    ) {
        val service = RetrofitClient.instance
        val call = service.downloadFile(fileUrl)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        saveFileToDisk(context, body, fileUrl, callback)
                    } ?: callback.onDownloadFailed("Response body is null")
                } else {
                    callback.onDownloadFailed("Failed to download file: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback.onDownloadFailed("Error: ${t.message}")
            }
        })
    }

    private fun saveFileToDisk(
        context: Context,
        body: ResponseBody,
        fileUrl: String,
        callback: DownloadCallback
    ) {
        try {
            val file = getGLBFile(context, fileUrl)

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                val fileSize = body.contentLength()
                val buffer = ByteArray(4096)
                var bytesRead: Int
                var downloadedSize: Long = 0

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    downloadedSize += bytesRead

                    // 更新下载进度
                    val progress = (downloadedSize * 100 / fileSize).toInt()
                    callback.onProgressUpdate(progress)
                }

                outputStream.flush()
                callback.onDownloadSuccess(file.absolutePath)
                Log.d("Download", "File downloaded: ${file.absolutePath}")
            } catch (e: Exception) {
                callback.onDownloadFailed("Failed to save the file: ${e.message}")
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: Exception) {
            callback.onDownloadFailed("Error: ${e.message}")
        }
    }

    private fun getGLBFileNameForURL(url: String): String {
        return url.substringAfterLast("/").substringBefore(".glb")
    }

    fun getGLBFile(context: Context, url: String): File {
        val path = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "glbfiles")
        if (!path.exists()) {
            path.mkdir()
        }
        val fileName = getGLBFileNameForURL(url)
        val nameWithExtenion = "$fileName.glb"
        return File(path, nameWithExtenion)
    }
}