package online.avogadro.opencv4tasker.app;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "MyAppPreferences";
    public static final String CLAUDE_API_KEY = "CLAUDE_API_KEY";
    private static final String PASSWORD = "u2fg393ujk.%!kspa5fg393ujk.%!kra"; // Not recommended

    public static void save(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String encryptedValue = encrypt(value, PASSWORD);
        prefs.edit().putString(key, encryptedValue).apply();
    }

    public static String get(Context context, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String val = prefs.getString(key, "");
        if ("".equals(val))
            return val;
        return decrypt(val, PASSWORD);
    }

    private static String encrypt(String value, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedValueBytes = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(encryptedValueBytes, Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String decrypt(String value, String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] originalValueBytes = cipher.doFinal(Base64.decode(value, Base64.DEFAULT));
            return new String(originalValueBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
