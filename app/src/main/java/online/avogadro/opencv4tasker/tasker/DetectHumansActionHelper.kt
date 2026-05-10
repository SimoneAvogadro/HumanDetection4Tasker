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
import online.avogadro.opencv4tasker.claudeai.HumansDetectorClaudeAI
import online.avogadro.opencv4tasker.databinding.ActivityConfigDetectHumansBinding
import online.avogadro.opencv4tasker.gemini.HumansDetectorGemini
import online.avogadro.opencv4tasker.gemma4.HumansDetectorGemma4
import online.avogadro.opencv4tasker.openrouter.HumansDetectorOpenRouter
import online.avogadro.opencv4tasker.app.Util
import online.avogadro.opencv4tasker.tensorflowlite.HumansDetectorTensorFlow

const val ENGINE_CLAUDEAI = "CLAUDE"
const val ENGINE_TENSORFLOW = "TENSORFLOW"
const val ENGINE_GEMINI = "GEMINI"
const val ENGINE_OPENROUTER = "OPENROUTER"
const val ENGINE_GEMMA4 = "GEMMA4"

class DetectHumansActionHelper(config: TaskerPluginConfig<DetectHumansInput>) : TaskerPluginConfigHelper<DetectHumansInput, DetectHumansOutput, DetectHumansActionRunner>(config) {
    override val runnerClass: Class<DetectHumansActionRunner> get() = DetectHumansActionRunner::class.java
    override val inputClass = DetectHumansInput::class.java
    override val outputClass = DetectHumansOutput::class.java
    override fun addToStringBlurb(input: TaskerInput<DetectHumansInput>, blurbBuilder: StringBuilder) {
        blurbBuilder.append(" detect humans in image")
    }
}

class ActivityConfigDetectHumansAction : Activity(), TaskerPluginConfig<DetectHumansInput> {

    private lateinit var binding: ActivityConfigDetectHumansBinding

    override fun assignFromInput(input: TaskerInput<DetectHumansInput>) {
        binding.editFileName.setText(input.regular.imagePath);

        // Reset all radio buttons
        binding.radioEngineClaudeAI.isChecked = false
        binding.radioEngineGemini.isChecked = false
        binding.radioEngineOpenRouter.isChecked = false
        binding.radioEngineTensorflowLite.isChecked = false
        binding.radioEngineGemma4.isChecked = false

        // Set the appropriate radio button based on the engine
        when (input.regular.engine) {
            ENGINE_CLAUDEAI -> binding.radioEngineClaudeAI.isChecked = true
            ENGINE_GEMINI -> binding.radioEngineGemini.isChecked = true
            ENGINE_OPENROUTER -> binding.radioEngineOpenRouter.isChecked = true
            ENGINE_GEMMA4 -> binding.radioEngineGemma4.isChecked = true
            else -> binding.radioEngineTensorflowLite.isChecked = true // default (backward compat)
        }

        // disable Claude options if there's no API KEY
        val claudeApiKey = SharedPreferencesHelper.get(this, SharedPreferencesHelper.CLAUDE_API_KEY)
        if (claudeApiKey.isEmpty()) {
            binding.radioEngineClaudeAI.isEnabled = false
            binding.radioEngineClaudeAI.isChecked = false
            if (ENGINE_CLAUDEAI == input.regular.engine) binding.radioEngineTensorflowLite.isChecked = true
        }

        // disable Gemini options if there's no API KEY
        val geminiApiKey = SharedPreferencesHelper.get(this, SharedPreferencesHelper.GEMINI_API_KEY)
        if (geminiApiKey.isEmpty()) {
            binding.radioEngineGemini.isEnabled = false
            binding.radioEngineGemini.isChecked = false
            if (ENGINE_GEMINI == input.regular.engine) binding.radioEngineTensorflowLite.isChecked = true
        }

        // disable OpenRouter options if there's no API KEY
        val openRouterApiKey = SharedPreferencesHelper.get(this, SharedPreferencesHelper.OPENROUTER_API_KEY)
        if (openRouterApiKey.isEmpty()) {
            binding.radioEngineOpenRouter.isEnabled = false
            binding.radioEngineOpenRouter.isChecked = false
            if (ENGINE_OPENROUTER == input.regular.engine) binding.radioEngineTensorflowLite.isChecked = true
        }

        // disable Gemma 4 if model file is not configured or not found
        if (!isGemma4Available()) {
            binding.radioEngineGemma4.isEnabled = false
            binding.radioEngineGemma4.isChecked = false
            if (ENGINE_GEMMA4 == input.regular.engine) binding.radioEngineTensorflowLite.isChecked = true
        }

    }

    private fun isGemma4Available(): Boolean {
        val path = SharedPreferencesHelper.get(this, SharedPreferencesHelper.GEMMA4_MODEL_PATH)
        return Util.isModelFileAccessible(path)
    }

    override val inputForTasker: TaskerInput<DetectHumansInput> get() {
        val engine = when {
            binding.radioEngineClaudeAI.isChecked -> ENGINE_CLAUDEAI
            binding.radioEngineGemini.isChecked -> ENGINE_GEMINI
            binding.radioEngineOpenRouter.isChecked -> ENGINE_OPENROUTER
            binding.radioEngineGemma4.isChecked -> ENGINE_GEMMA4
            else -> ENGINE_TENSORFLOW  // default (backward compat)
        }
        return TaskerInput<DetectHumansInput>(DetectHumansInput(binding.editFileName.text?.toString(), engine))
    }

    override val context get() = applicationContext
    private val taskerHelper by lazy { DetectHumansActionHelper(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityConfigDetectHumansBinding.inflate(layoutInflater)

        binding.buttonOK.setOnClickListener {
            // Handle button click event
            taskerHelper.finishForTasker()
        }
        setContentView(binding.root)
        taskerHelper.onCreate()
    }
}

class DetectHumansActionRunner : TaskerPluginRunnerAction<DetectHumansInput, DetectHumansOutput>() {
    override fun run(context: Context, input: TaskerInput<DetectHumansInput>): TaskerPluginResult<DetectHumansOutput> {
        var result: Int = 0
        var resultReason = "";
        var resultError = "";

        // Here the plugin EXECUTES
        if (ENGINE_CLAUDEAI.equals(input.regular.engine)) {
            // result = HumansDetectorClaudeAI.detectHumans(context, input.regular.imagePath);
            val htc = HumansDetectorClaudeAI()
            htc.setup(context)
            result = htc.detectPerson(context, input.regular.imagePath)
            resultReason = htc.getLastResponse()
            if (result==-1)
                resultError = htc.getLastError()
        } else if (ENGINE_GEMINI.equals(input.regular.engine)) {
            val htg = HumansDetectorGemini()
            htg.setup(context)
            result = htg.detectPerson(context, input.regular.imagePath)
            resultReason = htg.getLastResponse()
            if (result==-1)
                resultError = htg.getLastError()
        } else if (ENGINE_OPENROUTER.equals(input.regular.engine)) {
            val hto = HumansDetectorOpenRouter()
            hto.setup(context)
            result = hto.detectPerson(context, input.regular.imagePath)
            resultReason = hto.getLastResponse()
            if (result==-1)
                resultError = hto.getLastError()
        } else if (ENGINE_GEMMA4.equals(input.regular.engine)) {
            val hq = HumansDetectorGemma4()
            hq.setup(context)
            result = hq.detectPerson(context, input.regular.imagePath)
            resultReason = hq.getLastResponse()
            if (result==-1)
                resultError = hq.getLastError()
        } else {
            // default = TENSORFLOW
            var path = input.regular.imagePath;
            if (path==null)
                path="FAIL"
            result = HumansDetectorTensorFlow.detectHumans(context, path);
        }

        if (result == -1) {
            if (resultError.equals(""))
                return TaskerPluginResultErrorWithOutput(-1,"Failed to perform detection on "+input.regular.imagePath)
            else
                return TaskerPluginResultErrorWithOutput(-1,"Failed to perform detection on "+input.regular.imagePath+" "+resultError)
        } else {
            return TaskerPluginResultSucess(DetectHumansOutput(result, resultReason))
        }
    }
}
