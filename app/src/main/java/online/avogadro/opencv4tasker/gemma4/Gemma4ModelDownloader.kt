package online.avogadro.opencv4tasker.gemma4

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Downloads the Gemma 4 E2B IT multimodal LiteRT-LM bundle (~2.4 GB) from the
 * litert-community HuggingFace repo into the app's private filesDir. No storage
 * permissions needed.
 *
 * Single-shot per instance: create, call download(), observe via Listener, optionally cancel().
 */
class Gemma4ModelDownloader(private val context: Context) {

    interface Listener {
        fun onProgress(bytesRead: Long, totalBytes: Long)
        fun onComplete(file: File)
        fun onError(message: String)
        fun onCancelled()
    }

    private val cancelled = AtomicBoolean(false)
    private var future: Future<*>? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun cancel() {
        cancelled.set(true)
        future?.cancel(true)
    }

    fun download(listener: Listener) {
        future = executor.submit {
            val destFile = modelFile(context)
            val partial = File(destFile.parentFile, destFile.name + PARTIAL_SUFFIX)
            try {
                destFile.parentFile?.mkdirs()
                if (partial.exists()) partial.delete()

                val url = URL(MODEL_URL)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    connectTimeout = 30_000
                    readTimeout = 60_000
                    requestMethod = "GET"
                }
                try {
                    val code = conn.responseCode
                    if (code !in 200..299) {
                        post { listener.onError("HTTP $code from ${conn.url}") }
                        return@submit
                    }
                    val total = conn.contentLengthLong
                    if (total > 0) {
                        val needed = total + DISK_HEADROOM_BYTES
                        val available = destFile.parentFile?.usableSpace ?: 0L
                        if (available < needed) {
                            post {
                                listener.onError(
                                    "Not enough free space: need ${formatBytes(needed)}" +
                                        " (model ${formatBytes(total)} + ${formatBytes(DISK_HEADROOM_BYTES)} headroom)" +
                                        ", available ${formatBytes(available)}"
                                )
                            }
                            return@submit
                        }
                    }
                    val input = conn.inputStream
                    val output = partial.outputStream()
                    var bytesRead = 0L
                    var lastReported = 0L
                    val buffer = ByteArray(64 * 1024)
                    input.use { ins ->
                        output.use { outs ->
                            while (true) {
                                if (cancelled.get()) {
                                    post { listener.onCancelled() }
                                    return@submit
                                }
                                val n = ins.read(buffer)
                                if (n < 0) break
                                outs.write(buffer, 0, n)
                                bytesRead += n
                                if (bytesRead - lastReported > PROGRESS_REPORT_BYTES) {
                                    lastReported = bytesRead
                                    val br = bytesRead
                                    post { listener.onProgress(br, total) }
                                }
                            }
                        }
                    }
                    if (cancelled.get()) {
                        post { listener.onCancelled() }
                        return@submit
                    }
                    if (total > 0 && bytesRead != total) {
                        partial.delete()
                        post { listener.onError("Download truncated: $bytesRead / $total bytes") }
                        return@submit
                    }
                    if (destFile.exists()) destFile.delete()
                    if (!partial.renameTo(destFile)) {
                        post { listener.onError("Cannot finalize file: rename failed") }
                        return@submit
                    }
                    post { listener.onComplete(destFile) }
                } finally {
                    conn.disconnect()
                }
            } catch (e: InterruptedException) {
                partial.delete()
                post { listener.onCancelled() }
            } catch (e: IOException) {
                Log.e(TAG, "Download failed", e)
                partial.delete()
                if (cancelled.get()) post { listener.onCancelled() }
                else post { listener.onError(e.message ?: "I/O error") }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
                partial.delete()
                post { listener.onError(e.message ?: "Unknown error") }
            }
        }
    }

    private fun post(r: () -> Unit) {
        mainHandler.post(r)
    }

    companion object {
        private const val TAG = "Gemma4ModelDownloader"
        private const val PARTIAL_SUFFIX = ".partial"
        private const val PROGRESS_REPORT_BYTES = 1_000_000L
        private const val DISK_HEADROOM_BYTES = 100L * 1024L * 1024L
        const val MODEL_FILENAME = "gemma-4-E2B-it.litertlm"
        const val MODEL_URL =
            "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"

        @JvmStatic
        fun modelDir(context: Context): File =
            File(context.filesDir, "models")

        @JvmStatic
        fun modelFile(context: Context): File =
            File(modelDir(context), MODEL_FILENAME)

        @JvmStatic
        fun isModelPresent(context: Context): Boolean {
            val f = modelFile(context)
            return f.exists() && f.length() > 0
        }

        @JvmStatic
        fun deleteModel(context: Context): Boolean {
            val f = modelFile(context)
            return if (f.exists()) f.delete() else true
        }

        @JvmStatic
        fun formatBytes(bytes: Long): String = when {
            bytes <= 0L -> "?"
            bytes >= 1024L * 1024L * 1024L -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
