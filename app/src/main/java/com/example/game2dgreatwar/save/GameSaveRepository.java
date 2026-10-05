package com.example.game2dgreatwar.save;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Stores the in-progress game as a JSON file in the app's private storage.
 */
public final class GameSaveRepository {
    public static final int SAVE_VERSION = 1;
    public static final String KEY_VERSION = "version";

    private static final String TAG = "GameSaveRepository";
    private static final String FILE_NAME = "savegame.json";
    private static final String TEMP_FILE_NAME = "savegame.json.tmp";

    private final File saveFile;
    private final File tempFile;

    public GameSaveRepository(Context context) {
        File dir = context.getApplicationContext().getFilesDir();
        saveFile = new File(dir, FILE_NAME);
        tempFile = new File(dir, TEMP_FILE_NAME);
    }

    public boolean hasSave() {
        return saveFile.isFile();
    }

    /**
     * Writes to a temp file first and then renames it, so a kill in the middle of writing never
     * leaves a half-written save behind.
     */
    public synchronized boolean save(JSONObject state) {
        try {
            state.put(KEY_VERSION, SAVE_VERSION);
            try (OutputStream out = new FileOutputStream(tempFile)) {
                out.write(state.toString().getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            if (!tempFile.renameTo(saveFile)) {
                Log.e(TAG, "Could not move temp save into place");
                return false;
            }
            return true;
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Saving game failed", e);
            return false;
        }
    }

    /**
     * @return the saved state, or null when there is no usable save (a broken save is deleted)
     */
    public synchronized JSONObject load() {
        if (!hasSave()) {
            return null;
        }
        try (InputStream in = new FileInputStream(saveFile)) {
            byte[] bytes = new byte[(int) saveFile.length()];
            int offset = 0;
            while (offset < bytes.length) {
                int read = in.read(bytes, offset, bytes.length - offset);
                if (read < 0) {
                    break;
                }
                offset += read;
            }
            JSONObject state = new JSONObject(new String(bytes, 0, offset, StandardCharsets.UTF_8));
            if (state.optInt(KEY_VERSION) != SAVE_VERSION) {
                Log.w(TAG, "Ignoring save with unsupported version");
                delete();
                return null;
            }
            return state;
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Save file is broken, deleting it", e);
            delete();
            return null;
        }
    }

    public synchronized void delete() {
        if (saveFile.exists() && !saveFile.delete()) {
            Log.e(TAG, "Could not delete save file");
        }
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}
