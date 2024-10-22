package online.avogadro.opencv4tasker.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import online.avogadro.opencv4tasker.opencv.HumansDetector;

public class Util {

    private static final String TAG = "Util";

    public static String getMetadata(Context c, String key) {
        try {
            ApplicationInfo ai = c.getPackageManager().getApplicationInfo(c.getPackageName(),
                    PackageManager.GET_META_DATA);

            Bundle metaData = ai.metaData;

            return metaData.getString(key, "8");
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getMetadataInt(Context c, String key) {
        try {
            ApplicationInfo ai = c.getPackageManager().getApplicationInfo(c.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle metaData = ai.metaData;

            return metaData.getInt(key,8);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

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
}
