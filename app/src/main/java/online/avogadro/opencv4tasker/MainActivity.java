package online.avogadro.opencv4tasker;

import androidx.appcompat.app.AppCompatActivity;
import online.avogadro.opencv4tasker.app.SharedPreferencesHelper;
import online.avogadro.opencv4tasker.app.Util;
import online.avogadro.opencv4tasker.claudeai.HumansDetectorClaudeAI;
import online.avogadro.opencv4tasker.gemini.HumansDetectorGemini;
import online.avogadro.opencv4tasker.gemma4.HumansDetectorGemma4;
import online.avogadro.opencv4tasker.openrouter.HumansDetectorOpenRouter;
import online.avogadro.opencv4tasker.tensorflowlite.HumansDetectorTensorFlow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;

// import org.opencv.android.OpenCVLoader;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    static final String ENGINE_GEMINI = "GEMINI";
    static final String ENGINE_OPENROUTER = "OPENROUTER";
    static final String ENGINE_GEMMA4 = "GEMMA4";

    EditText testImagePath;

    HumansDetectorTensorFlow h = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage!=null) {
                String imagePath = selectedImage.toString();
                testImagePath.setText(imagePath);
                // Save the selected image path to SharedPreferences
                SharedPreferencesHelper.save(this, SharedPreferencesHelper.LAST_IMAGE_PATH, imagePath);
                Log.d(TAG,"New file: " + imagePath);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check Claude API key and enable/disable Claude option
        RadioButton claudeButton = findViewById(R.id.radioEngineClaudeAI);
        boolean hasClaudeKey = !"".equals(SharedPreferencesHelper.get(this, SharedPreferencesHelper.CLAUDE_API_KEY));
        claudeButton.setEnabled(hasClaudeKey);
        
        // Check Gemini API key and enable/disable Gemini option
        RadioButton geminiButton = findViewById(R.id.radioEngineGemini);
        boolean hasGeminiKey = !"".equals(SharedPreferencesHelper.get(this, SharedPreferencesHelper.GEMINI_API_KEY));
        geminiButton.setEnabled(hasGeminiKey);
        
        // Check OpenRouter API key and enable/disable OpenRouter option
        RadioButton openRouterButton = findViewById(R.id.radioEngineOpenRouter);
        boolean hasOpenRouterKey = !"".equals(SharedPreferencesHelper.get(this, SharedPreferencesHelper.OPENROUTER_API_KEY));
        openRouterButton.setEnabled(hasOpenRouterKey);
        
        // Gemma 4: enabled only when the local model is downloaded
        RadioButton gemma4Button = findViewById(R.id.radioEngineGemma4);
        String gemma4Path = SharedPreferencesHelper.get(this, SharedPreferencesHelper.GEMMA4_MODEL_PATH);
        boolean hasGemma4 = Util.isModelFileAccessible(gemma4Path);
        gemma4Button.setEnabled(hasGemma4);
        gemma4Button.setText(hasGemma4 ? R.string.engine_gemma4 : R.string.engine_gemma4_disabled);

        // Show/hide warning message if API keys are missing
        TextView warningTextView = findViewById(R.id.warningTextView);
        if (!hasClaudeKey || !hasGeminiKey || !hasOpenRouterKey) {
            warningTextView.setVisibility(View.VISIBLE);
        } else {
            warningTextView.setVisibility(View.GONE);
        }

        // Set click listener for warning message to open instructions URL
        warningTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/464a1Dm"));
                startActivity(browserIntent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        testImagePath = findViewById(R.id.testImagePath);

        // Restore the last selected image path from SharedPreferences
        String lastImagePath = SharedPreferencesHelper.get(this, SharedPreferencesHelper.LAST_IMAGE_PATH);
        if (lastImagePath != null && !lastImagePath.isEmpty()) {
            testImagePath.setText(lastImagePath);
        }

        // Check Claude API key and enable/disable Claude option
        RadioButton claudeButton = findViewById(R.id.radioEngineClaudeAI);
        boolean hasClaudeKey = !"".equals(SharedPreferencesHelper.get(this, SharedPreferencesHelper.CLAUDE_API_KEY));
        claudeButton.setEnabled(hasClaudeKey);

        // Check Gemini API key and enable/disable Gemini option
        RadioButton geminiButton = findViewById(R.id.radioEngineGemini);
        boolean hasGeminiKey = !"".equals(SharedPreferencesHelper.get(this, SharedPreferencesHelper.GEMINI_API_KEY));
        geminiButton.setEnabled(hasGeminiKey);

        // Check OpenRouter API key and enable/disable OpenRouter option
        RadioButton openRouterButton = findViewById(R.id.radioEngineOpenRouter);
        boolean hasOpenRouterKey = !"".equals(SharedPreferencesHelper.get(this, SharedPreferencesHelper.OPENROUTER_API_KEY));
        openRouterButton.setEnabled(hasOpenRouterKey);

        // Gemma 4: enabled only when the local model is downloaded
        RadioButton gemma4Button = findViewById(R.id.radioEngineGemma4);
        String gemma4Path = SharedPreferencesHelper.get(this, SharedPreferencesHelper.GEMMA4_MODEL_PATH);
        boolean hasGemma4 = Util.isModelFileAccessible(gemma4Path);
        gemma4Button.setEnabled(hasGemma4);
        gemma4Button.setText(hasGemma4 ? R.string.engine_gemma4 : R.string.engine_gemma4_disabled);

        // Show/hide warning message if API keys are missing
        TextView warningTextView = findViewById(R.id.warningTextView);
        if (!hasClaudeKey || !hasGeminiKey || !hasOpenRouterKey) {
            warningTextView.setVisibility(View.VISIBLE);
        } else {
            warningTextView.setVisibility(View.GONE);
        }

        // Set click listener for warning message to open instructions URL
        warningTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/464a1Dm"));
                startActivity(browserIntent);
            }
        });

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
                
                // Try to set initial URI to current image path
                String currentPath = testImagePath.getText().toString();
                if (!currentPath.isEmpty()) {
                    try {
                        Uri initialUri = Uri.parse(currentPath);
                        // For Android 8.0 (API 26) and higher
                        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri);
                        Log.d(TAG, "Setting initial URI: " + initialUri);
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting initial URI: " + e.getMessage());
                    }
                }
                
                startActivityForResult(intent, PICK_IMAGE);
            }
        });
    }

    private void processImage(String imageUri) {
        TextView resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText("processing...");
        String engine;
        
        // Determine which engine is selected
        RadioButton radioTensorflow = (RadioButton)findViewById(R.id.radioEngineTensorflowLite);
        RadioButton radioGemini = (RadioButton)findViewById(R.id.radioEngineGemini);
        RadioButton radioClaude = (RadioButton)findViewById(R.id.radioEngineClaudeAI);
        RadioButton radioOpenRouter = (RadioButton)findViewById(R.id.radioEngineOpenRouter);
        RadioButton radioGemma4 = (RadioButton)findViewById(R.id.radioEngineGemma4);

        int detectionScore = -99;
        if (radioTensorflow.isChecked()) {
            engine = ENGINE_TENSORFLOW;
        } else if (radioGemini.isChecked()) {
            engine = ENGINE_GEMINI;
        } else if (radioOpenRouter.isChecked()) {
            engine = ENGINE_OPENROUTER;
        } else if (radioGemma4.isChecked()) {
            engine = ENGINE_GEMMA4;
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
            } else if (engine==ENGINE_GEMINI) {
                // Use Gemini API
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(() -> {
                    try {
                        // Background work here
                        HumansDetectorGemini h = new HumansDetectorGemini();
                        h.setup(this);
                        int result = h.detectPerson(this, imageUri);

                        handler.post(() -> {
                            // UI Thread work here
                            if (result!=-1)
                                resultTextView.setText("Detection score: "+result+" "+ENGINE_GEMINI+"\n"+h.getLastResponse());
                            else
                                resultTextView.setText("Detection failure: "+h.getLastError());
                        });
                    } catch (IOException e) {
                        handler.post(() -> {
                            resultTextView.setText("Detection error: " + e.getMessage());
                        });
                    }
                });
            } else if (engine==ENGINE_OPENROUTER) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(() -> {
                    try {
                        // Background work here
                        HumansDetectorOpenRouter h = new HumansDetectorOpenRouter();
                        h.setup(this);
                        int result = h.detectPerson(this, imageUri);

                        handler.post(() -> {
                            // UI Thread work here
                            if (result!=-1)
                                resultTextView.setText("Detection score: "+result+" "+ENGINE_OPENROUTER+"\n"+h.getLastResponse());
                            else
                                resultTextView.setText("Detection failure: "+h.getLastError());
                        });
                    } catch (IOException e) {
                        handler.post(() -> {
                            resultTextView.setText("Detection error: " + e.getMessage());
                        });
                    }
                });
            } else if (engine==ENGINE_GEMMA4) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(() -> {
                    try {
                        HumansDetectorGemma4 h = new HumansDetectorGemma4();
                        h.setup(this);
                        int result = h.detectPerson(this, imageUri);

                        handler.post(() -> {
                            if (result!=-1)
                                resultTextView.setText("Detection score: "+result+" "+ENGINE_GEMMA4+"\n"+h.getLastResponse());
                            else
                                resultTextView.setText("Detection failure: "+h.getLastError());
                        });
                    } catch (IOException e) {
                        handler.post(() -> {
                            resultTextView.setText("Detection error: " + e.getMessage());
                        });
                    }
                });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
