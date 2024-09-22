package online.avogadro.opencv4tasker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import online.avogadro.opencv4tasker.app.SharedPreferencesHelper;
import online.avogadro.opencv4tasker.tensorflowlite.HumansDetectorTensorFlow;

public class ConfigActivity extends AppCompatActivity {

    private static final String TAG = "ConfigActivity";

    EditText claudeApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        claudeApiKey = findViewById(R.id.claudeApiKey);
        String val = SharedPreferencesHelper.get(this,SharedPreferencesHelper.CLAUDE_API_KEY);
        if (val!=null)
            claudeApiKey.setText(val);

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferencesHelper.save(
                        ConfigActivity.this,
                        SharedPreferencesHelper.CLAUDE_API_KEY,
                        claudeApiKey.getText().toString() );
                finish(); // return to main activity
            }
        });

    }
}
