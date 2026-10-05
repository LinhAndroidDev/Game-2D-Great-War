package com.example.game2dgreatwar;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

/**
 * Plays short sound effects from a preloaded SoundPool. Creating a MediaPlayer per sound blocks
 * the calling thread for 15-40ms, which made the game stutter on every hit/pickup.
 */
public final class SoundManager {
    private static final int[] SOUND_IDS = {
            R.raw.sound_shoot,
            R.raw.sound_hit_enemy,
            R.raw.sound_enemy_attack,
            R.raw.sound_health,
            R.raw.sound_coin_recieved,
            R.raw.sound_coin_appear
    };

    private static SoundPool soundPool;
    private static final Map<Integer, Integer> loadedSounds = new HashMap<>();

    private SoundManager() {
    }

    public static synchronized void init(Context context) {
        if (soundPool != null) {
            return;
        }
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(attributes)
                .build();
        Context appContext = context.getApplicationContext();
        for (int soundId : SOUND_IDS) {
            loadedSounds.put(soundId, soundPool.load(appContext, soundId, 1));
        }
    }

    public static void play(int soundId) {
        SoundPool pool = soundPool;
        if (pool == null) {
            return;
        }
        Integer streamSoundId = loadedSounds.get(soundId);
        if (streamSoundId != null) {
            pool.play(streamSoundId, 1f, 1f, 1, 0, 1f);
        }
    }
}
