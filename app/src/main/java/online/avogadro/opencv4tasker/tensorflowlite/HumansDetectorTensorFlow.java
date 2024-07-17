package online.avogadro.opencv4tasker.tensorflowlite;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.task.vision.detector.Detection;
import org.tensorflow.lite.task.vision.detector.ObjectDetector;


import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import online.avogadro.opencv4tasker.app.Util;

public class HumansDetectorTensorFlow {
    private ObjectDetector objectDetector;
    static final String TAG = "HumansDetectorTensorFlow";

    public void setup(Context ctx) throws IOException {
        objectDetector = ObjectDetector.createFromBufferAndOptions(
                loadModelFile(ctx),
                ObjectDetector.ObjectDetectorOptions.builder()
                        .setMaxResults(5)
                        .setScoreThreshold(0.5f)
                        .build());
    }

    private MappedByteBuffer loadModelFile(Context ctx) throws IOException {
        AssetFileDescriptor fileDescriptor = ctx.getAssets().openFd("lite-model_efficientdet_lite0_detection_metadata_1.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public int detectPerson(Context ctx, String imagePath) {
        String newPath = null;
        try {
            newPath = Util.contentToFile(ctx,imagePath);
            // Load image from disk
            Bitmap bitmap = BitmapFactory.decodeFile(newPath);

            // Convert bitmap to TensorImage
            TensorImage image = TensorImage.fromBitmap(bitmap);

            // Run inference
            List<Detection> results = objectDetector.detect(image);

            // Process results
            float highestScore = 0f;
            for (Detection detection : results) {
                if (detection.getCategories().get(0).getLabel().equals("person")) {
                    float score = detection.getCategories().get(0).getScore();
                    if (score > highestScore) {
                        highestScore = score;
                    }
                }
            }

            // Convert the highest score to an integer in the range 0-100
            return Math.round(highestScore * 100);
        } catch (IOException e) {
            Log.e(TAG, "Failed to parse file name "+newPath,e);
            return -1;
        } finally {
            if (newPath!=null && !newPath.equals(newPath))
                new File(newPath).delete();
        }

    }
}
