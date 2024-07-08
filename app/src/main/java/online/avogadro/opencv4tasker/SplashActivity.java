package online.avogadro.opencv4tasker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import online.avogadro.opencv4tasker.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        verifyBatteryPermission();
        verifyStoragePermissions(this);
    }

    // TODO: does not seem to work
    //   1 - if opening permissions intent must not switch to home page
    //   2 - even when not switching to homepage does not seem to open the system dialog to provide the permission
    private void verifyBatteryPermission() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        String packageName = getPackageName();
        boolean isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName);

        if (!isIgnoringBatteryOptimizations) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static final String[] PERMISSIONS_ALL =
            {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                    ,Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ,Manifest.permission.READ_MEDIA_IMAGES
                    ,Manifest.permission.FOREGROUND_SERVICE
                    ,Manifest.permission.MANAGE_EXTERNAL_STORAGE
            };

    private void verifyStoragePermissions(Activity activity) {
        try {
            int READ_EXTERNAL_STORAGE = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            int MANAGE_EXTERNAL_STORAGE = ActivityCompat.checkSelfPermission(activity, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            int READ_MEDIA_IMAGES = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES);
            //int START_FOREGROUND_SERVICES_FROM_BACKGROUND = ActivityCompat.checkSelfPermission(activity, Manifest.permission.START_FOREGROUND_SERVICES_FROM_BACKGROUND);
            int FOREGROUND_SERVICE = ActivityCompat.checkSelfPermission(activity, Manifest.permission.FOREGROUND_SERVICE);
            if (       READ_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED
                    || MANAGE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED
                    || READ_MEDIA_IMAGES != PackageManager.PERMISSION_GRANTED
                    // || READ_MEDIA_IMAGES != PackageManager.PERMISSION_GRANTED
                    || FOREGROUND_SERVICE != PackageManager.PERMISSION_GRANTED
            ) { // || RECORD_AUDIO != PackageManager.PERMISSION_GRANTED
                ActivityCompat.requestPermissions(activity, PERMISSIONS_ALL,REQUEST_EXTERNAL_STORAGE);
            } else {
                goHome();
            }
        } catch (Exception e) {
            Log.e("SplashActivity","error verifying permissions: "+e.getMessage(),e);
            Toast.makeText(this, "Failed to check permissions", Toast.LENGTH_LONG).show();
            // e.printStackTrace();
            goHome();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                goHome();
            } else {
                Log.e("SplashActivity","missing persmissions");
                goHome();
            }
        } else {
            Log.e("SplashActivity","wrong request code");
            goHome();
        }
    }

    private void goHome() {
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent;
//                loginWithStoredCredentials(SplashActivity.this, new ILoginCallback() {
//                    @Override
//                    public void onSuccess(UserInfo userInfo) {
//                        Toast.makeText(SplashActivity.this,"Autologin ok", Toast.LENGTH_LONG).show();
//                        goToActivity(MainActivity.class);
//                    }
//
//                    @Override
//                    public void onError(int i, String s) {
//                        Toast.makeText(SplashActivity.this, R.string.toast_fail+" autologin: "+i+" s:"+s, Toast.LENGTH_LONG).show();
//                        goToActivity(LoginActivity.class);
//                    }
//                });
//            }
//        },100);
        goToActivity(MainActivity.class);
    }

    private void goToActivity(Class dest) {
        Intent intent = new Intent(this, dest);
        startActivity(intent);
        // finish();    // No more: la dobbiamo lasciare attiva
    }

//    public static void loginWithStoredCredentials(Context ctx, ILoginCallback then) {
//        String username = SharedPreferencesHelper.get(ctx,"username");
//        String password = SharedPreferencesHelper.get(ctx,"password");
//
//        if ("".equals(username) || "".equals(password)) {
//            then.onError(-1,"No stored login data");
//            return; // no stored credentials, go on with standard login
//        }
//
//        MeariSmartSdk.partnerId= AWS4TaskerApplication.partnerIdS;
//        MeariUser.getInstance().loginWithAccount("country", "code", username, password, new ILoginCallback() {
//            @Override
//            public void onSuccess(UserInfo userInfo) {
//                // MyFirebaseMessagingService.startListening(SplashActivity.this);
//                if (then!=null)
//                    then.onSuccess(userInfo);
//            }
//
//            @Override
//            public void onError(int i, String s) {
//                SharedPreferencesHelper.save(ctx, "username", "" );
//                SharedPreferencesHelper.save(ctx, "password", "" );
//                if (then!=null)
//                    then.onError(i,s);
//            }
//        } );
//    }
}
