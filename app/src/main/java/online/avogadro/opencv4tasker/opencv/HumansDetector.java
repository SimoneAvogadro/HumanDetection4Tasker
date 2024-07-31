package online.avogadro.opencv4tasker.opencv;

import android.content.Context;
import android.util.Log;

//import org.opencv.android.OpenCVLoader;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfDouble;
//import org.opencv.core.MatOfRect;
//import org.opencv.core.Rect;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.IOException;

import online.avogadro.opencv4tasker.app.Util;

public class HumansDetector {
//
//    private static final String TAG = "HumansDetector";
//
//    /**
//     * Detect humans and return the highest score
//     * @param path in the form of file:///{something} or content:///{something}
//     * @return 0-100+, lower values are lower scores. '-1' is a failure
//     */
//    public static int detectHumans(Context context, String path) {
//        if (!OpenCVLoader.initLocal()) {
//            Log.e(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
//            // Toast.makeText(this,"FAILED TO INIT OpenCV",Toast.LENGTH_SHORT).show();
//            return -1;
//        } else {
//            Log.d(TAG, "OpenCV library found inside package. Using it!");
//        }
//
//        String newPath = null;
//        try {
//            newPath = Util.contentToFile(context,path);
//            return detectHumansFromFile(newPath);
//        } catch (IOException e) {
//            Log.e(TAG, "Failed to parse file name "+path,e);
//            return -1;
//        } finally {
//            if (newPath!=null && !path.equals(newPath))
//                new File(newPath).delete();
//        }
//    }
//
//    /**
//         * Detect humans and return the highest score
//         * @param path in the form of file:///{something}
//         * @return 0-100+, lower values are lower scores. '-1' is a failure
//         */
//    private static int detectHumansFromFile(String path) {
//        try {
//            // Load the image from Uri
//            Mat image = Imgcodecs.imread(path);
//
//            if (image.empty()) {
//                if (!new File(path).canRead())
//                    Log.e(TAG,"File does not exist or missing access rights");
//                else if (!Imgcodecs.haveImageReader(path))
//                    Log.e(TAG,"File exists but image format is unknown");
//                else
//                    Log.e(TAG,"Failed to parse the image in the existing file");
//
//                return -1;
//            }
//
//            // Create HOG descriptor and set SVM detector
//            HOGDescriptor hog = new HOGDescriptor();
//            hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());
//
//            // Detect humans in the image
//            MatOfRect detections = new MatOfRect();
//            MatOfDouble foundWeights = new MatOfDouble();
//            hog.detectMultiScale(image, detections, foundWeights);
//
//            Rect[] rects = detections.toArray();
//            if (rects.length==0) {
//                Log.d(TAG,"nothing detected");
//                return 0;
//            }
//            double[] weights = foundWeights.toArray();
//
//            double max=0;
//            for (int i = 0; i < rects.length; i++) {
//                max = Math.max(max,weights[i]);
//            }
//
//            return (int)(max*100);
//
//        } catch (Exception e) {
//            Log.e(TAG,"error processing image: "+e.getMessage(),e);
//            return -1;
//        }
//    }
}
