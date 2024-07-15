package online.avogadro.opencv4tasker.googleml

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.File

object HumansDetectorGoogleML {
    private val objectDetector by lazy {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        ObjectDetection.getClient(options)
    }


    fun detectPersonBoundingBoxes(
        image: InputImage,
        onSuccess: (List<Rect>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        objectDetector.process(image)
            .addOnSuccessListener { detectedObjects ->
                val personBounds = detectedObjects
                    .filter { it.labels.any { label -> label.text == "Person" } }
                    .map { it.boundingBox }
                onSuccess(personBounds)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun detectPersonConfidence(
        image: InputImage,
        onSuccess: (Float) -> Unit,
        onError: (Exception) -> Unit
    ) {
        objectDetector.process(image)
            .addOnSuccessListener { detectedObjects ->
                val highestConfidence = detectedObjects
                    .flatMap { it.labels }
                    .filter { it.text == "Person" }
                    .maxOf { it.confidence }
                onSuccess(highestConfidence)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun detectPersonConfidence(
        context: Context,
        imagePath: String,
        onSuccess: (Float) -> Unit,
        onError: (Exception) -> Unit
    ) {
        detectPersonConfidence(
            InputImage.fromFilePath(context, Uri.fromFile(File(imagePath))),
            onSuccess,
            onError);
    }

    fun detectPersonConfidence(
        context: Context,
        imagePath: String
    ): Int {
        var confidence=-1f;
        var exception:Exception?=null;
        detectPersonConfidence(
            InputImage.fromFilePath(context, Uri.fromFile(File(imagePath))),
            { c -> confidence=c },
            { e -> exception = e });
        if (exception!=null)
            throw exception as Exception

        return (confidence*100).toInt()
    }

}