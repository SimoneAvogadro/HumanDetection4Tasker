package online.avogadro.opencv4tasker.opencv;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.HOGDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HumansDetector {

    private static final String TAG = "HumansDetector";

    public static String getPathFromUri(Context context, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        InputStream inputStream = resolver.openInputStream(uri);

        // Create temporary file
        File tempFile = File.createTempFile("temp_image", ".jpg", context.getCacheDir());
        tempFile.deleteOnExit();

        // Copy input stream to temporary file
        FileOutputStream out = new FileOutputStream(tempFile);
        byte[] buffer = new byte[64*1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
        out.close();
        inputStream.close();

        return tempFile.getAbsolutePath();
    }

    /**
     * Detect humans and return the highest score
     * @param path in the form of file:///{something} or content:///{something}
     * @return 0-100+, lower values are lower scores. '-1' is a failure
     */
    public static int detectHumans(Context context, String path) {
        String newPath = null;
        try {
            newPath = contentToFile(context,path);
            return detectHumansFromFile(newPath);
        } catch (IOException e) {
            Log.e(TAG, "Failed to parse file name "+path,e);
            return -1;
        } finally {
            if (newPath!=null && !path.equals(newPath))
                new File(newPath).delete();
        }
//        if (path.startsWith("file:")) {
//            return detectHumansFromFile(path);
//        } else if (path.startsWith("content:")) {
//            String filePath = "";
//            try {
//                filePath = getPathFromUri(context, Uri.parse(path));
//                return detectHumansFromFile(filePath);
//            } catch (IOException e) {
//                Log.e(TAG, "Failed to parse file name "+path,e);
//                return -1;
//            } finally {
//                new File(filePath).delete();
//            }
//        } else {
//            Log.d(TAG,"formato path sconosciuto");
//            return -1;
//        }
    }

    /**
     * OpenCV and other libs are unable to handle content:// URIs
     * This method handls this for them by copying to a temporary file
     *
     * @param path
     * @return
     */
    public static String contentToFile(Context context, String path) throws IOException {
        if (path.startsWith("file:")) {
            return path;
        } else if (path.startsWith("content:")) {
            return getPathFromUri(context, Uri.parse(path));
        } else {
            Log.w(TAG,"formato path sconosciuto");
            return path;
        }

    }

    /**
         * Detect humans and return the highest score
         * @param path in the form of file:///{something}
         * @return 0-100+, lower values are lower scores. '-1' is a failure
         */
    private static int detectHumansFromFile(String path) {
        try {
            // Load the image from Uri
            Mat image = Imgcodecs.imread(path);

            if (image.empty()) {
                if (!new File(path).canRead())
                    Log.e(TAG,"File does not exist or missing access rights");
                else if (!Imgcodecs.haveImageReader(path))
                    Log.e(TAG,"File exists but image format is unknown");
                else
                    Log.e(TAG,"Failed to parse the image in the existing file");

                return -1;
            }

            // Create HOG descriptor and set SVM detector
            HOGDescriptor hog = new HOGDescriptor();
            hog.setSVMDetector(HOGDescriptor.getDefaultPeopleDetector());

            // Detect humans in the image
            MatOfRect detections = new MatOfRect();
            MatOfDouble foundWeights = new MatOfDouble();
            hog.detectMultiScale(image, detections, foundWeights);

            Rect[] rects = detections.toArray();
            if (rects.length==0) {
                Log.d(TAG,"nothing detected");
                return 0;
            }
            double[] weights = foundWeights.toArray();

            double max=0;
            for (int i = 0; i < rects.length; i++) {
                max = Math.max(max,weights[i]);
            }

            return (int)(max*100);

        } catch (Exception e) {
            Log.e(TAG,"error processing image: "+e.getMessage(),e);
            return -1;
        }
    }
}
