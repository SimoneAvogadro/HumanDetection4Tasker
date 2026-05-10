package online.avogadro.opencv4tasker.tasker

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultErrorWithOutput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import online.avogadro.opencv4tasker.app.SharedPreferencesHelper
import online.avogadro.opencv4tasker.app.Util
import online.avogadro.opencv4tasker.claudeai.HumansDetectorClaudeAI
import online.avogadro.opencv4tasker.databinding.ActivityConfigAnalyzeImageBinding
import online.avogadro.opencv4tasker.gemini.HumansDetectorGemini
import online.avogadro.opencv4tasker.gemma4.HumansDetectorGemma4
import online.avogadro.opencv4tasker.openrouter.HumansDetectorOpenRouter
import java.io.File

const val ENGINE_ANALYZE_CLAUDEAI = "CLAUDE"
const val ENGINE_ANALYZE_GEMINI = "GEMINI"
const val ENGINE_ANALYZE_OPENROUTER = "OPENROUTER"
const val ENGINE_ANALYZE_GEMMA4 = "GEMMA4"

class AnalyzeImageActionHelper(config: TaskerPluginConfig<AnalyzeImageInput>) : TaskerPluginConfigHelper<AnalyzeImageInput, AnalyzeImageOutput, AnalyzeImageActionRunner>(config) {
    override val runnerClass: Class<AnalyzeImageActionRunner> get() = AnalyzeImageActionRunner::class.java
    override val inputClass = AnalyzeImageInput::class.java
    override val outputClass = AnalyzeImageOutput::class.java
    override fun addToStringBlurb(input: TaskerInput<AnalyzeImageInput>, blurbBuilder: StringBuilder) {
        blurbBuilder.append(" analyze image with AI")
    }
}

class ActivityConfigAnalyzeImageAction : Activity(), TaskerPluginConfig<AnalyzeImageInput> {

    private lateinit var binding: ActivityConfigAnalyzeImageBinding

    override fun assignFromInput(input: TaskerInput<AnalyzeImageInput>) {
        binding.editImagePath.setText(input.regular.imagePath ?: "")
        binding.editSystemPrompt.setText(input.regular.systemPrompt ?: "")
        binding.editUserPrompt.setText(input.regular.userPrompt ?: "")

        // Reset all radio buttons
        binding.radioEngineClaudeAI.isChecked = false
        binding.radioEngineGemini.isChecked = false
        binding.radioEngineOpenRouter.isChecked = false
        binding.radioEngineGemma4.isChecked = false

        // Set the appropriate radio button based on the engine
        when (input.regular.engine) {
            ENGINE_ANALYZE_CLAUDEAI -> binding.radioEngineClaudeAI.isChecked = true
            ENGINE_ANALYZE_GEMINI -> binding.radioEngineGemini.isChecked = true
            ENGINE_ANALYZE_OPENROUTER -> binding.radioEngineOpenRouter.isChecked = true
            ENGINE_ANALYZE_GEMMA4 -> binding.radioEngineGemma4.isChecked = true
            else -> {
                // Default to Claude if available, else Gemma 4, else OpenRouter, else Gemini
                when {
                    isClaudeAvailable() -> binding.radioEngineClaudeAI.isChecked = true
                    isGemma4Available() -> binding.radioEngineGemma4.isChecked = true
                    isOpenRouterAvailable() -> binding.radioEngineOpenRouter.isChecked = true
                    else -> binding.radioEngineGemini.isChecked = true
                }
            }
        }

        // Disable Claude option if no API KEY is available
        if (!isClaudeAvailable()) {
            binding.radioEngineClaudeAI.isEnabled = false
            binding.radioEngineClaudeAI.isChecked = false
            if (ENGINE_ANALYZE_CLAUDEAI == input.regular.engine) binding.radioEngineGemini.isChecked = true
        }

        // Disable Gemini option if no API KEY is available
        if (!isGeminiAvailable()) {
            binding.radioEngineGemini.isEnabled = false
            binding.radioEngineGemini.isChecked = false
            if (ENGINE_ANALYZE_GEMINI == input.regular.engine) binding.radioEngineClaudeAI.isChecked = true
        }

        // Disable OpenRouter option if no API KEY is available
        if (!isOpenRouterAvailable()) {
            binding.radioEngineOpenRouter.isEnabled = false
            binding.radioEngineOpenRouter.isChecked = false
            if (ENGINE_ANALYZE_OPENROUTER == input.regular.engine) {
                if (isClaudeAvailable()) binding.radioEngineClaudeAI.isChecked = true
                else binding.radioEngineGemini.isChecked = true
            }
        }

        // Disable Gemma 4 if model file is not configured or not found
        if (!isGemma4Available()) {
            binding.radioEngineGemma4.isEnabled = false
            binding.radioEngineGemma4.isChecked = false
            if (ENGINE_ANALYZE_GEMMA4 == input.regular.engine) {
                if (isClaudeAvailable()) binding.radioEngineClaudeAI.isChecked = true
                else binding.radioEngineGemini.isChecked = true
            }
        }

    }

    private fun isClaudeAvailable(): Boolean {
        val claudeApiKey = SharedPreferencesHelper.get(this, SharedPreferencesHelper.CLAUDE_API_KEY)
        return claudeApiKey.isNotEmpty()
    }

    private fun isGeminiAvailable(): Boolean {
        val geminiApiKey = SharedPreferencesHelper.get(this, SharedPreferencesHelper.GEMINI_API_KEY)
        return geminiApiKey.isNotEmpty()
    }

    private fun isOpenRouterAvailable(): Boolean {
        val openRouterApiKey = SharedPreferencesHelper.get(this, SharedPreferencesHelper.OPENROUTER_API_KEY)
        return openRouterApiKey.isNotEmpty()
    }

    private fun isGemma4Available(): Boolean {
        val path = SharedPreferencesHelper.get(this, SharedPreferencesHelper.GEMMA4_MODEL_PATH)
        return Util.isModelFileAccessible(path)
    }

    override val inputForTasker: TaskerInput<AnalyzeImageInput> get() {
        val engine = when {
            binding.radioEngineClaudeAI.isChecked -> ENGINE_ANALYZE_CLAUDEAI
            binding.radioEngineGemini.isChecked -> ENGINE_ANALYZE_GEMINI
            binding.radioEngineOpenRouter.isChecked -> ENGINE_ANALYZE_OPENROUTER
            binding.radioEngineGemma4.isChecked -> ENGINE_ANALYZE_GEMMA4
            else -> ENGINE_ANALYZE_CLAUDEAI // Default to Claude
        }
        
        return TaskerInput(AnalyzeImageInput(
            binding.editImagePath.text?.toString(),
            engine,
            binding.editSystemPrompt.text?.toString(),
            binding.editUserPrompt.text?.toString()
        ))
    }

    override val context get() = applicationContext
    private val taskerHelper by lazy { AnalyzeImageActionHelper(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigAnalyzeImageBinding.inflate(layoutInflater)

        binding.buttonOK.setOnClickListener {
            taskerHelper.finishForTasker()
        }
        
        setContentView(binding.root)
        taskerHelper.onCreate()
    }
}

class AnalyzeImageActionRunner : TaskerPluginRunnerAction<AnalyzeImageInput, AnalyzeImageOutput>() {
    override fun run(context: Context, input: TaskerInput<AnalyzeImageInput>): TaskerPluginResult<AnalyzeImageOutput> {
        var response = ""
        var error = ""
        var newPath: String? = null
        
        try {
            // Get the image path
            val imagePath = input.regular.imagePath ?: return TaskerPluginResultErrorWithOutput(
                -1, "No image path provided"
            )
            
            // Convert content:// URIs to file paths if needed
            newPath = Util.contentToFile(context, imagePath)
            
            // Select the appropriate AI engine
            when (input.regular.engine) {
                ENGINE_ANALYZE_CLAUDEAI -> {
                    val claude = HumansDetectorClaudeAI()
                    claude.setup(context)
                    response = claude.analyzeImage(
                        input.regular.systemPrompt ?: "",
                        input.regular.userPrompt,
                        newPath
                    )
                    if (response.isEmpty()) {
                        error = claude.getLastError()
                    }
                }
                ENGINE_ANALYZE_GEMINI -> {
                    val gemini = HumansDetectorGemini()
                    gemini.setup(context)
                    response = gemini.analyzeImage(
                        input.regular.systemPrompt ?: "",
                        input.regular.userPrompt,
                        newPath
                    )
                    if (response.isEmpty()) {
                        error = gemini.getLastError()
                    }
                }
                ENGINE_ANALYZE_OPENROUTER -> {
                    val openRouter = HumansDetectorOpenRouter()
                    openRouter.setup(context)
                    response = openRouter.analyzeImage(
                        input.regular.systemPrompt ?: "",
                        input.regular.userPrompt,
                        newPath
                    )
                    if (response.isEmpty()) {
                        error = openRouter.getLastError()
                    }
                }
                ENGINE_ANALYZE_GEMMA4 -> {
                    val gemma4 = HumansDetectorGemma4()
                    gemma4.setup(context)
                    response = gemma4.analyzeImage(
                        input.regular.systemPrompt ?: "",
                        input.regular.userPrompt,
                        newPath
                    )
                    if (response.isEmpty()) {
                        error = gemma4.getLastError()
                    }
                }
                else -> {
                    return TaskerPluginResultErrorWithOutput(
                        -1, "Invalid engine selected: ${input.regular.engine}"
                    )
                }
            }
            
            // Check if we got a valid response
            if (response.isEmpty()) {
                return TaskerPluginResultErrorWithOutput(
                    -1, "Failed to analyze image: $error"
                )
            }
            
            return TaskerPluginResultSucess(AnalyzeImageOutput(response))
            
        } catch (e: Exception) {
            return TaskerPluginResultErrorWithOutput(
                -1, "Error analyzing image: ${e.message}"
            )
        } finally {
            // Clean up temporary file if created
            if (newPath != null && newPath != input.regular.imagePath) {
                File(newPath).delete()
            }
        }
    }
}
