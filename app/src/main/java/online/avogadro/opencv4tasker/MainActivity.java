package online.avogadro.opencv4tasker;

import androidx.appcompat.app.AppCompatActivity;
import online.avogadro.opencv4tasker.app.SharedPreferencesHelper;
import online.avogadro.opencv4tasker.claudeai.HumansDetectorClaudeAI;
import online.avogadro.opencv4tasker.tensorflowlite.HumansDetectorTensorFlow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

// import org.opencv.android.OpenCVLoader;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HumanDetectionActivity";
    private static final int PICK_IMAGE = 1;

    static final String ENGINE_CLAUDE_AI = "CLAUDE";
    static final String ENGINE_TENSORFLOW = "TENSORFLOW";

    EditText testImagePath;

    HumansDetectorTensorFlow h = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage!=null) {
                testImagePath.setText(selectedImage.toString());
                Log.d(TAG,"New file: "+selectedImage.toString());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        RadioButton claudeButton = findViewById(R.id.radioEngineClaudeAI);
        if ("".equals(SharedPreferencesHelper.get(this, SharedPreferencesHelper.CLAUDE_API_KEY)))
            claudeButton.setEnabled(false);
        else
            claudeButton.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testImagePath = findViewById(R.id.testImagePath);

        RadioButton claudeButton = findViewById(R.id.radioEngineClaudeAI);
        if ("".equals(SharedPreferencesHelper.get(this, SharedPreferencesHelper.CLAUDE_API_KEY)))
            claudeButton.setEnabled(false);
        else
            claudeButton.setEnabled(true);

        findViewById(R.id.buttonTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "file:///sdcard/Pictures/Image1719668651.jpg"
                // "content://media/external/images/media/1000000033"
                // "/document/image:1000000033"

                // "file:///sdcard/Pictures/Image1719684270.jpg"
                // "content://media/external/images/media/1000000035"
                // "/document/image:1000000035"

                // contains person
                // "file:///sdcard/Pictures/Image1720166835.jpg"
                // "content://media/external/images/media/1000000081"
                // "/document/image:1000000081"


                // processImage("file:///sdcard/Pictures/Image1719668651.jpg");
                processImage(testImagePath.getText().toString());
            }
        });
        findViewById(R.id.buttonPickFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });
        findViewById(R.id.buttonSetup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent);
            }
        });
    }

    private void processImage(String imageUri) {
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText("processing...");
        String engine = ENGINE_CLAUDE_AI;
        RadioButton radioGoogle =(RadioButton)findViewById(R.id.radioEngineTensorflowLite);

        int detectionScore = -99;
        if (radioGoogle.isChecked()) {
            engine = ENGINE_TENSORFLOW;
        } else {
            engine = ENGINE_CLAUDE_AI;
        }
//            ExecutorService executor = Executors.newSingleThreadExecutor();
//            Handler handler = new Handler(Looper.getMainLooper());
//
//            executor.execute(() -> {
//                // Background work here
//                int result = HumansDetectorGoogleML.INSTANCE.detectPersonConfidence(this, imageUri);
//
//                handler.post(() -> {
//                    // UI Thread work here
//                    resultTextView.setText("Detection score: "+result+" "+ENGINE_GOOGLEML);
//                });
//            });
            // detectionScore = HumansDetectorGoogleML.INSTANCE.detectPersonConfidence(this, imageUri);
        try {
            if (engine==ENGINE_TENSORFLOW) {
                if (h==null) {
                    h = new HumansDetectorTensorFlow();
                    h.setup(this);
                }
                detectionScore = h.detectPerson(this, imageUri);
                if (detectionScore==-1) {
                    resultTextView.setText("Failed to execute detection");
                    return;
                } else {
                    resultTextView.setText("Detection score: "+detectionScore+" "+engine);
                }
            } else {    // default = Claude
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(() -> {
                    try {
                        // Background work here
                        HumansDetectorClaudeAI h = new HumansDetectorClaudeAI();
                        h.setup(this);
                        int result = h.detectPerson(this, imageUri);

                        handler.post(() -> {
                            // UI Thread work here
                            if (result!=-1)
                                resultTextView.setText("Detection score: "+result+" "+ENGINE_CLAUDE_AI+"\n"+h.lastResponse);
                            else
                                resultTextView.setText("Detection failure: "+h.lastHttpResponse+"\n"+ h.lastException);
                        });
                    } catch (IOException e) {
                        handler.post(() -> {
                            resultTextView.setText("Detection error: " + e.getMessage());
                        });
                    }
                });

            }
        } catch (IOException e) {
            resultTextView.setText("Failed to execute detection "+e.getMessage());
        }

//            StringBuilder result = new StringBuilder();
//            result.append("Number of humans detected: ").append(rects.length).append("\n\n");
//
//            for (int i = 0; i < rects.length; i++) {
//                result.append("Human ").append(i + 1).append(":\n");
//                result.append("Confidence: ").append(String.format("%.2f", weights[i])).append("\n");
//                result.append("Position: (").append(rects[i].x).append(", ").append(rects[i].y).append(")\n");
//                result.append("Size: ").append(rects[i].width).append("x").append(rects[i].height).append("\n\n");
//            }

    }

}
