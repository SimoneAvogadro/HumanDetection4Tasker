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
import online.avogadro.opencv4tasker.databinding.ActivityConfigDetectHumansBinding;
import online.avogadro.opencv4tasker.googleml.HumansDetectorGoogleML
import online.avogadro.opencv4tasker.opencv.HumansDetector
import online.avogadro.opencv4tasker.tensorflowlite.HumansDetectorTensorFlow

const val ENGINE_OPENCV = "OPENCV"
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

        if (ENGINE_TENSORFLOW.equals(input.regular.engine)) {
            binding?.radioEngineOpenCV?.isChecked=false;
            binding?.radioEngineGoogleML?.isChecked=true;
        } else { // null or anything else
            binding?.radioEngineOpenCV?.isChecked=true;
            binding?.radioEngineGoogleML?.isChecked=false;
        }
    }

    override val inputForTasker: TaskerInput<DetectHumansInput> get() {
        var engine = ENGINE_OPENCV
        if (binding?.radioEngineGoogleML?.isChecked()==true)
            engine = ENGINE_TENSORFLOW
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

        // Here the plugin EXECUTES
        if (ENGINE_TENSORFLOW.equals(input.regular.engine)) {
            var path = input.regular.imagePath;
            if (path==null)
                path="FAIL"
            result = HumansDetectorTensorFlow.detectHumans(context, path);
        } else { // in any other case use default = OpenCV
            result = HumansDetector.detectHumans(context, input.regular.imagePath);
        }

        if (result == -1) {
            return TaskerPluginResultErrorWithOutput(-1,"Failed to perform detection on "+input.regular.imagePath)
        } else {
            return TaskerPluginResultSucess(DetectHumansOutput(result))
        }
    }
}