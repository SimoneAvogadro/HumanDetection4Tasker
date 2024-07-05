package online.avogadro.opencv4tasker.tasker

import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

@TaskerOutputObject
class DetectHumansOutput(
        @get:TaskerOutputVariable("detectionScore") var detectionScore: Int? =0
) {
}