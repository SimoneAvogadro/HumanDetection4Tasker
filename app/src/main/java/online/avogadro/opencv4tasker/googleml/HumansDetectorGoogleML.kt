package online.avogadro.opencv4tasker.googleml

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import online.avogadro.opencv4tasker.app.Util
import online.avogadro.opencv4tasker.opencv.HumansDetector
import java.io.File
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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
        var newPath: String? = null
        return try {
            newPath = Util.contentToFile(context, imagePath)
            detectPersonConfidenceInFile(context, newPath)
        } catch (e: IOException) {
            Log.e("HumansDetectorGoogleML", "Failed to parse file name $imagePath", e)
            -1
        } finally {
            if (newPath != null && imagePath != newPath) File(newPath).delete()
        }
    }

    fun detectPersonConfidenceInFile(
        context: Context,
        imagePath: String
    ): Int {
        var confidence=-1f;
        var exception:Exception?=null;
        val latch = CountDownLatch(1)
        val u = Uri.fromFile(File(imagePath))
        val img = InputImage.fromFilePath(context, u)
        detectPersonConfidence(
            img,
            { c ->
                confidence=c
                latch.countDown()
            },
            { e ->
                exception = e
                latch.countDown()
            });

        latch.await(10, TimeUnit.SECONDS)

        if (exception!=null)
            throw exception as Exception

        if (confidence==-1f)
            return -1;
        else
            return (confidence*100).toInt()
    }

}