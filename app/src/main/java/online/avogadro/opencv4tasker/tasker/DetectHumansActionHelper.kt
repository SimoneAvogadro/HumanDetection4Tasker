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
import online.avogadro.opencv4tasker.databinding.ActivityConfigDetectHumansBinding;
import online.avogadro.opencv4tasker.tensorflowlite.HumansDetectorTensorFlow

const val ENGINE_CLAUDEAI = "CLAUDE"
const val ENGINE_TENSORFLOW = "TENSORFLOW"

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
        binding?.editFileName?.setText(input.regular.imagePath);

        if (ENGINE_CLAUDEAI.equals(input.regular.engine)) {
            binding?.radioEngineClaudeAI?.isChecked=true;
            binding?.radioEngineTensorflowLite?.isChecked=false;
        } else { // null or anything else
            // Local Tensorflow == default (backward compatibility!)
            binding?.radioEngineClaudeAI?.isChecked=false;
            binding?.radioEngineTensorflowLite?.isChecked=true;
        }

        // disable Claude options if there's no API KEY
        var apiKey = SharedPreferencesHelper.get(this, SharedPreferencesHelper.CLAUDE_API_KEY)
        if ("".equals(apiKey)) {
            binding?.radioEngineClaudeAI?.isEnabled = false
            binding?.radioEngineClaudeAI?.isChecked = false
            binding?.radioEngineTensorflowLite?.isChecked=true
        }
    }

    override val inputForTasker: TaskerInput<DetectHumansInput> get() {
        var engine = ENGINE_TENSORFLOW  // fail-safe local default
        if (binding?.radioEngineClaudeAI?.isChecked()==true)
            engine = ENGINE_CLAUDEAI
        return TaskerInput<DetectHumansInput>(DetectHumansInput(binding?.editFileName?.text?.toString(),engine))
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
                resultError = htc.lastError
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