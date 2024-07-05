package online.avogadro.opencv4tasker.tasker

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
class DetectHumansInput @JvmOverloads constructor(
        @field:TaskerInputField("imagePath") var imagePath: String? = null
)