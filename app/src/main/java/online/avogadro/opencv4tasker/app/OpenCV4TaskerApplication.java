package online.avogadro.opencv4tasker.app;

import android.app.Application;


public class OpenCV4TaskerApplication extends Application {

    public static int partnerId = 8;
    public static String partnerIdS = "8";

    private static OpenCV4TaskerApplication instance;

    public static OpenCV4TaskerApplication getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }
}
