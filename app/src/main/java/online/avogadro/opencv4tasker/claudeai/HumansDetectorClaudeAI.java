package online.avogadro.opencv4tasker.claudeai;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Base64;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import online.avogadro.opencv4tasker.app.SharedPreferencesHelper;
import online.avogadro.opencv4tasker.app.Util;

import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HumansDetectorClaudeAI {

    private String API_KEY = "YOUR_API_KEY_HERE";
    private static final String API_URL = "https://api.anthropic.com/v1/messages";

    private static final String PROMPT_SYSTEM = "The user will be providing images taken from cheap security cameras, these images might be taken during the day or the night and the angle may vary. Please reply him with a single keyword, chosen among these:\\n* HUMAN: an human or a part of an human is visible in the frame\\n* SPIDER: no humans are visible but a spider is near the camera\\n* NONE: neither an human nor a spider are in frame\\n* UNCERTAIN: you were unable to tell in which of the above categories the image might fit. Use this response if you are not totally sure that the answer is one of the above";

    static final String TAG = "HumansDetectorClaudeAI";
    public static final String CLAUDE_MODEL = "claude-3-5-sonnet-20240620";

    /**
     * Detect humans and return the highest score
     * @param path in the form of file:///{something} or content:///{something}
     * @return 0-100+, lower values are lower scores. '-1' is a failure
     */
    public static int detectHumans(Context context, String path) throws IOException {
        HumansDetectorClaudeAI htc = new HumansDetectorClaudeAI();
        htc.setup(context);
        return htc.detectPerson(context,path);
    }

    public void setup(Context ctx) throws IOException {
       API_KEY = SharedPreferencesHelper.get(ctx, SharedPreferencesHelper.CLAUDE_API_KEY);
    }


    public int detectPerson(Context ctx, String imagePath) {
        String newPath = null;
        try {
            newPath = Util.contentToFile(ctx,imagePath);
            String claudeResponse = makeApiCall(PROMPT_SYSTEM, null, newPath);
            if (claudeResponse.equals("HUMAN"))
                return 100;
            else if (claudeResponse.equals("NONE"))
                return 0;
            else if (claudeResponse.equals("SPIDER"))
                return 0;
            else
                return -1;  // issues

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to examine file "+newPath,e);
            return -1;
        } finally {
            if (newPath!=null && !newPath.equals(imagePath))
                new File(newPath).delete();
        }

    }

    private String makeApiCall(String systemPrompt, String userPrompt, String imagePath) throws IOException, JSONException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-API-Key", API_KEY);
        connection.setRequestProperty("anthropic-version","2023-06-01");
        connection.setDoOutput(true);

        String imageBase64 = encodeImageToBase64(imagePath);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("model", CLAUDE_MODEL);
        jsonBody.put("max_tokens", 1000);
        jsonBody.put("temperature", 0.0f);
        jsonBody.put("system", systemPrompt);

        JSONArray messages = new JSONArray();

//        JSONObject systemMessage = new JSONObject();
//        systemMessage.put("role", "system");
//        systemMessage.put("content", systemPrompt);
//        messages.put(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");

        JSONArray contentArray = new JSONArray();
        if (userPrompt!=null)
            contentArray.put(new JSONObject().put("type", "text").put("text", userPrompt));

        contentArray.put(new JSONObject().put("type", "image").put("source", new JSONObject().put("type", "base64").put("media_type", "image/jpeg").put("data", imageBase64)));

        userMessage.put("content", contentArray);
        messages.put(userMessage);

        jsonBody.put("messages", messages);

        OutputStream os = connection.getOutputStream();
        os.write(jsonBody.toString().getBytes());
        os.flush();
        os.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray ja = jsonResponse.getJSONArray("content");
            for (int i=0; i<ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                if ("text".equals(jo.getString("type"))) {
                    return jo.getString("text");
                }
            }
            throw new IOException("No text response found: "+response.toString());
            // return jsonResponse.getJSONObject("content").getString("text");
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String r = response.toString();
            throw new IOException("Error "+responseCode+" "+r);
            // return "Error: " + responseCode;
        }
    }
    private String encodeImageToBase64(String imagePath) throws IOException {
        File file = new File(imagePath);
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = 0;
            int offset = 0;
            while (offset < bytes.length && (bytesRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += bytesRead;
            }
            if (offset != bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
