package online.avogadro.opencv4tasker.gemma4

import android.content.Context
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import online.avogadro.opencv4tasker.ai.AIImageAnalyzer
import online.avogadro.opencv4tasker.app.SharedPreferencesHelper
import online.avogadro.opencv4tasker.app.Util
import org.json.JSONException
import java.io.File
import java.io.IOException

class HumansDetectorGemma4 : AIImageAnalyzer {

    companion object {
        private const val TAG = "HumansDetectorGemma4"

        private const val PROMPT_SYSTEM =
            "The user will be providing images taken from cheap security cameras, these images might be taken during the day or the night and the angle may vary. Images are usually taken top-down, during the night images may be blurry due to person's movements. Please reply him with a single keyword in the first line and a brief explanation of your choice in the second line, chosen among these:\n" +
            "* HUMAN: an human or a part of an human (usually on the border of the image) is visible in the frame. The human may be seen from above since the camera is usually mounted on an high position\n" +
            "* SPIDER: no humans are visible but a spider is near the camera\n" +
            "* CAT: if it's an animal or a cat, it may be a cat walking away from the camera or walking toward the camera\n" +
            "* NONE: neither an human nor a spider are in frame\n" +
            "* UNCERTAIN: you were unable to tell in which of the above categories the image might fit. Use this response if you are not totally sure that the answer is one of the above\n" +
            "Ignore any shadows"

        @Volatile
        private var engineInstance: Engine? = null
        private var engineModelPath: String? = null

        fun closeModel() {
            try {
                engineInstance?.close()
            } catch (_: Exception) { }
            engineInstance = null
            engineModelPath = null
        }
    }

    private var modelPath: String = ""
    private var cacheDir: String = ""
    private var lastResponse: String? = null
    private var lastError: String? = null

    @Throws(IOException::class)
    override fun setup(ctx: Context) {
        val path = SharedPreferencesHelper.get(ctx, SharedPreferencesHelper.GEMMA4_MODEL_PATH)
        if (path.isNullOrEmpty()) {
            throw IOException("Gemma 4 model not configured. Please download it from Settings.")
        }
        if (!Util.isModelFileAccessible(path)) {
            throw IOException("Gemma 4 model file not found at: $path")
        }
        modelPath = path
        cacheDir = ctx.applicationContext.cacheDir.absolutePath
    }

    override fun getLastResponse(): String = lastResponse ?: ""

    override fun getLastError(): String = lastError ?: ""

    @Throws(IOException::class, JSONException::class)
    override fun analyzeImage(systemPrompt: String, userPrompt: String?, imagePath: String): String {
        lastResponse = null
        lastError = null
        try {
            val engine = getOrCreateEngine()
            val effectiveText = if (!userPrompt.isNullOrEmpty()) userPrompt else systemPrompt
            val config = if (!userPrompt.isNullOrEmpty()) {
                ConversationConfig(systemInstruction = Contents.of(systemPrompt))
            } else {
                ConversationConfig()
            }
            val userMessage = Contents.of(Content.ImageFile(imagePath), Content.Text(effectiveText))
            engine.createConversation(config).use { conversation ->
                val response = conversation.sendMessage(userMessage).toString()
                lastResponse = response
                return response
            }
        } catch (oom: OutOfMemoryError) {
            val msg = "Out of memory loading Gemma 4 E2B (requires ~3 GB RAM). Close other apps and retry."
            Log.e(TAG, msg, oom)
            lastError = msg
            closeModel()
            return ""
        } catch (e: Exception) {
            Log.e(TAG, "Gemma 4 inference error", e)
            lastError = e.message ?: "Unknown error"
            return ""
        }
    }

    fun detectPerson(ctx: Context, imagePath: String?): Int {
        var newPath: String? = null
        return try {
            if (imagePath == null) {
                lastError = "imagePath is null"
                return -1
            }
            newPath = Util.contentToFile(ctx, imagePath)
            val response = analyzeImage(PROMPT_SYSTEM, null, newPath)
            if (response.isEmpty()) return -1
            lastResponse = response
            val firstLine = response.split("\n").firstOrNull()?.trim() ?: ""
            when (firstLine) {
                "HUMAN" -> 100
                "NONE", "SPIDER", "CAT" -> 0
                "UNCERTAIN" -> 30
                else -> -1
            }
        } catch (e: Exception) {
            Log.e(TAG, "detectPerson failed", e)
            lastError = e.message ?: "Unknown error"
            -1
        } finally {
            if (newPath != null && newPath != imagePath) {
                File(newPath).delete()
            }
        }
    }

    private fun getOrCreateEngine(): Engine {
        if (modelPath.isEmpty()) throw IOException("Call setup() before analyzeImage()")
        engineInstance?.let { existing ->
            if (engineModelPath == modelPath) return existing
            Log.i(TAG, "Model path changed, reloading engine")
            try { existing.close() } catch (_: Exception) { }
            engineInstance = null
        }
        Log.i(TAG, "Loading Gemma 4 E2B from: $modelPath (first load may take 60-90s)")
        // Try GPU first (Adreno on Snapdragon → faster than CPU); fall back to CPU
        // if the model bundle has no GPU kernels or the device lacks libOpenCL.so.
        val engine = tryInitEngine(Backend.GPU(), Backend.GPU(), "GPU")
            ?: tryInitEngine(Backend.CPU(), Backend.CPU(), "CPU")
            ?: throw IOException("Cannot initialize Gemma 4 engine on either GPU or CPU backend.")
        engineInstance = engine
        engineModelPath = modelPath
        return engine
    }

    private fun tryInitEngine(backend: Backend, visionBackend: Backend, label: String): Engine? {
        return try {
            val config = EngineConfig(
                modelPath = modelPath,
                backend = backend,
                visionBackend = visionBackend,
                maxNumImages = 1,
                cacheDir = cacheDir,
            )
            val engine = Engine(config)
            engine.initialize()
            Log.i(TAG, "Gemma 4 engine initialized on $label backend")
            engine
        } catch (e: Throwable) {
            Log.w(TAG, "$label backend init failed: ${e.message}")
            null
        }
    }
}
