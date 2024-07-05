package online.avogadro.opencv4tasker;

import androidx.appcompat.app.AppCompatActivity;
import online.avogadro.opencv4tasker.R;
import online.avogadro.opencv4tasker.opencv.HumansDetector;

import android.os.Bundle;

import org.opencv.android.OpenCVLoader;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HumanDetectionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initLocal()) {
            Log.e(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            Toast.makeText(this,"FAILED TO INIT OpenCV",Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
        }

        Button testRecognition = findViewById(R.id.buttonTest);
        testRecognition.setOnClickListener(new View.OnClickListener() {
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
                EditText testImagePath = findViewById(R.id.testImagePath);
                processImage(testImagePath.getText().toString());
            }
        });
    }

    private void processImage(String imageUri) {
        TextView resultTextView = findViewById(R.id.resultTextView);

        int detectionScore = HumansDetector.detectHumans(this, imageUri);
        if (detectionScore==-1) {
            resultTextView.setText("Failed to execute detection");
            return;
        }

        resultTextView.setText("Detection score: "+detectionScore);

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
