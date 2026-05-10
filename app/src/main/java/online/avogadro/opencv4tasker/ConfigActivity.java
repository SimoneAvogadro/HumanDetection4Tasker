package online.avogadro.opencv4tasker;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import online.avogadro.opencv4tasker.app.SharedPreferencesHelper;
import online.avogadro.opencv4tasker.gemma4.Gemma4ModelDownloader;

import java.io.File;

public class ConfigActivity extends AppCompatActivity {

    private static final String TAG = "ConfigActivity";

    // Known model IDs for the spinners. The last entry ("") signals Custom.
    private static final String[] CLAUDE_MODEL_IDS = {
            "claude-sonnet-4-6",
            "claude-opus-4-6",
            "claude-sonnet-4-5",
            "claude-haiku-4-5",
            ""
    };
    private static final String[] GEMINI_MODEL_IDS = {
            "gemini-2.5-flash",
            "gemini-2.5-pro",
            "gemini-3-flash-preview",
            "gemini-3.1-pro-preview",
            ""
    };
    /** Deprecated model ID kept for backward-compat migration. */
    private static final String GEMINI_DEPRECATED_3_PRO = "gemini-3-pro-preview";
    private static final String GEMINI_REPLACEMENT_3_PRO = "gemini-3.1-pro-preview";

    EditText claudeApiKey;
    Spinner  claudeModelSpinner;
    EditText claudeCustomModel;

    EditText geminiApiKey;
    Spinner  geminiModelSpinner;
    EditText geminiCustomModel;

    EditText openRouterApiKey;
    EditText openRouterModel;

    private TextView gemma4StatusLabel;
    private ProgressBar gemma4ProgressBar;
    private Button gemma4DownloadBtn;
    private Button gemma4DeleteBtn;
    private Gemma4ModelDownloader gemma4Downloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // --- Claude API Key ---
        claudeApiKey = findViewById(R.id.claudeApiKey);
        String claudeVal = SharedPreferencesHelper.get(this, SharedPreferencesHelper.CLAUDE_API_KEY);
        if (claudeVal != null) claudeApiKey.setText(claudeVal);

        // --- Claude Model Spinner ---
        claudeModelSpinner = findViewById(R.id.claudeModelSpinner);
        claudeCustomModel  = findViewById(R.id.claudeCustomModel);

        String[] claudeNames = {
                getString(R.string.claude_model_sonnet_46),
                getString(R.string.claude_model_opus_46),
                getString(R.string.claude_model_sonnet_45),
                getString(R.string.claude_model_haiku_45),
                getString(R.string.model_custom)
        };
        ArrayAdapter<String> claudeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, claudeNames);
        claudeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        claudeModelSpinner.setAdapter(claudeAdapter);

        String savedClaudeModel = SharedPreferencesHelper.get(this, SharedPreferencesHelper.CLAUDE_MODEL);
        if (savedClaudeModel == null || savedClaudeModel.isEmpty()) {
            claudeModelSpinner.setSelection(0); // nessuna preferenza salvata → default (Sonnet 4.6)
        } else {
            int claudeIdx = findModelIndex(savedClaudeModel, CLAUDE_MODEL_IDS);
            if (claudeIdx >= 0) {
                claudeModelSpinner.setSelection(claudeIdx);
            } else {
                claudeModelSpinner.setSelection(CLAUDE_MODEL_IDS.length - 1); // Custom
                claudeCustomModel.setText(savedClaudeModel);
                claudeCustomModel.setVisibility(View.VISIBLE);
            }
        }
        claudeModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                claudeCustomModel.setVisibility(
                        position == CLAUDE_MODEL_IDS.length - 1 ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- Gemini API Key ---
        geminiApiKey = findViewById(R.id.geminiApiKey);
        String geminiVal = SharedPreferencesHelper.get(this, SharedPreferencesHelper.GEMINI_API_KEY);
        if (geminiVal != null) geminiApiKey.setText(geminiVal);

        // --- Gemini Model Spinner ---
        geminiModelSpinner = findViewById(R.id.geminiModelSpinner);
        geminiCustomModel  = findViewById(R.id.geminiCustomModel);

        String[] geminiNames = {
                getString(R.string.gemini_model_25_flash),
                getString(R.string.gemini_model_25_pro),
                getString(R.string.gemini_model_30_flash_preview),
                getString(R.string.gemini_model_31_pro_preview),
                getString(R.string.model_custom)
        };
        ArrayAdapter<String> geminiAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, geminiNames);
        geminiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        geminiModelSpinner.setAdapter(geminiAdapter);

        String savedGeminiModel = SharedPreferencesHelper.get(this, SharedPreferencesHelper.GEMINI_MODEL);
        if (GEMINI_DEPRECATED_3_PRO.equals(savedGeminiModel)) {
            savedGeminiModel = GEMINI_REPLACEMENT_3_PRO;
            SharedPreferencesHelper.save(this, SharedPreferencesHelper.GEMINI_MODEL, savedGeminiModel);
        }
        if (savedGeminiModel == null || savedGeminiModel.isEmpty()) {
            geminiModelSpinner.setSelection(0); // nessuna preferenza salvata → default (2.5 Flash)
        } else {
            int geminiIdx = findModelIndex(savedGeminiModel, GEMINI_MODEL_IDS);
            if (geminiIdx >= 0) {
                geminiModelSpinner.setSelection(geminiIdx);
            } else {
                geminiModelSpinner.setSelection(GEMINI_MODEL_IDS.length - 1); // Custom
                geminiCustomModel.setText(savedGeminiModel);
                geminiCustomModel.setVisibility(View.VISIBLE);
            }
        }
        geminiModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                geminiCustomModel.setVisibility(
                        position == GEMINI_MODEL_IDS.length - 1 ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- OpenRouter ---
        openRouterApiKey = findViewById(R.id.openRouterApiKey);
        String openRouterKeyVal = SharedPreferencesHelper.get(this, SharedPreferencesHelper.OPENROUTER_API_KEY);
        if (openRouterKeyVal != null) openRouterApiKey.setText(openRouterKeyVal);

        openRouterModel = findViewById(R.id.openRouterModel);
        String openRouterModelVal = SharedPreferencesHelper.get(this, SharedPreferencesHelper.OPENROUTER_MODEL);
        if (openRouterModelVal != null && !openRouterModelVal.isEmpty())
            openRouterModel.setText(openRouterModelVal);
        else
            openRouterModel.setText(SharedPreferencesHelper.DEFAULT_OPENROUTER_MODEL);

        // --- Gemma 4 0.8B (local model, on-demand download) ---
        gemma4StatusLabel = findViewById(R.id.gemma4ModelStatusLabel);
        gemma4ProgressBar = findViewById(R.id.gemma4ProgressBar);
        gemma4DownloadBtn = findViewById(R.id.buttonGemma4Download);
        gemma4DeleteBtn = findViewById(R.id.buttonGemma4Delete);
        updateGemma4UiState();
        gemma4DownloadBtn.setOnClickListener(v -> startGemma4Download());
        gemma4DeleteBtn.setOnClickListener(v -> deleteGemma4Model());

        // --- Save button ---
        findViewById(R.id.buttonSave).setOnClickListener(v -> {
            SharedPreferencesHelper.save(this, SharedPreferencesHelper.CLAUDE_API_KEY,
                    claudeApiKey.getText().toString());

            int claudeSel = claudeModelSpinner.getSelectedItemPosition();
            String claudeModel = (claudeSel == CLAUDE_MODEL_IDS.length - 1)
                    ? claudeCustomModel.getText().toString().trim()
                    : CLAUDE_MODEL_IDS[claudeSel];
            SharedPreferencesHelper.save(this, SharedPreferencesHelper.CLAUDE_MODEL, claudeModel);

            SharedPreferencesHelper.save(this, SharedPreferencesHelper.GEMINI_API_KEY,
                    geminiApiKey.getText().toString());

            int geminiSel = geminiModelSpinner.getSelectedItemPosition();
            String geminiModel = (geminiSel == GEMINI_MODEL_IDS.length - 1)
                    ? geminiCustomModel.getText().toString().trim()
                    : GEMINI_MODEL_IDS[geminiSel];
            SharedPreferencesHelper.save(this, SharedPreferencesHelper.GEMINI_MODEL, geminiModel);

            SharedPreferencesHelper.save(this, SharedPreferencesHelper.OPENROUTER_API_KEY,
                    openRouterApiKey.getText().toString());
            SharedPreferencesHelper.save(this, SharedPreferencesHelper.OPENROUTER_MODEL,
                    openRouterModel.getText().toString());

            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gemma4Downloader != null) {
            gemma4Downloader.cancel();
            gemma4Downloader = null;
        }
    }

    private void updateGemma4UiState() {
        boolean present = Gemma4ModelDownloader.isModelPresent(this);
        gemma4ProgressBar.setVisibility(View.GONE);
        if (present) {
            gemma4StatusLabel.setText(R.string.gemma4_ready);
            gemma4DownloadBtn.setVisibility(View.GONE);
            gemma4DeleteBtn.setVisibility(View.VISIBLE);
        } else {
            gemma4StatusLabel.setText(R.string.gemma4_not_downloaded);
            gemma4DownloadBtn.setVisibility(View.VISIBLE);
            gemma4DeleteBtn.setVisibility(View.GONE);
        }
    }

    private void startGemma4Download() {
        // Hide both buttons during the download itself; updateGemma4UiState()
        // restores visibility based on file presence after the run terminates.
        gemma4DownloadBtn.setVisibility(View.GONE);
        gemma4DeleteBtn.setVisibility(View.GONE);
        gemma4ProgressBar.setProgress(0);
        gemma4ProgressBar.setVisibility(View.VISIBLE);
        gemma4StatusLabel.setText(getString(R.string.gemma4_downloading, 0, "0", "?"));
        gemma4Downloader = new Gemma4ModelDownloader(this);
        gemma4Downloader.download(new Gemma4ModelDownloader.Listener() {
            @Override
            public void onProgress(long bytesRead, long totalBytes) {
                int pct = totalBytes > 0 ? (int) (100L * bytesRead / totalBytes) : 0;
                gemma4ProgressBar.setProgress(pct);
                gemma4StatusLabel.setText(getString(
                        R.string.gemma4_downloading,
                        pct,
                        Gemma4ModelDownloader.formatBytes(bytesRead),
                        Gemma4ModelDownloader.formatBytes(totalBytes)));
            }

            @Override
            public void onComplete(File file) {
                SharedPreferencesHelper.save(ConfigActivity.this,
                        SharedPreferencesHelper.GEMMA4_MODEL_PATH, file.getAbsolutePath());
                gemma4Downloader = null;
                updateGemma4UiState();
            }

            @Override
            public void onError(String message) {
                gemma4Downloader = null;
                updateGemma4UiState();
                gemma4StatusLabel.setText(getString(R.string.gemma4_download_failed, message));
            }

            @Override
            public void onCancelled() {
                gemma4Downloader = null;
                updateGemma4UiState();
                gemma4StatusLabel.setText(R.string.gemma4_download_cancelled);
            }
        });
    }

    private void deleteGemma4Model() {
        Gemma4ModelDownloader.deleteModel(this);
        SharedPreferencesHelper.save(this, SharedPreferencesHelper.GEMMA4_MODEL_PATH, "");
        updateGemma4UiState();
        Toast.makeText(this, R.string.gemma4_deleted, Toast.LENGTH_SHORT).show();
    }

    /** Returns the index of modelId in modelIds (excluding the last "Custom" entry), or -1. */
    private int findModelIndex(String modelId, String[] modelIds) {
        if (modelId == null || modelId.isEmpty()) return -1;
        for (int i = 0; i < modelIds.length - 1; i++) {
            if (modelIds[i].equals(modelId)) return i;
        }
        return -1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
