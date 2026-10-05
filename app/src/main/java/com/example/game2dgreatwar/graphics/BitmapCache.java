package com.example.game2dgreatwar.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Decodes each drawable once per target size and reuses it, so spawning objects during gameplay
 * never decodes images on the game thread.
 */
public final class BitmapCache {
    private static final Map<Long, Bitmap> cache = new HashMap<>();

    private BitmapCache() {
    }

    public static synchronized Bitmap get(Context context, int resId, int size) {
        long key = ((long) resId << 32) | size;
        Bitmap bitmap = cache.get(key);
        if (bitmap == null) {
            bitmap = decodeScaled(context, resId, size);
            cache.put(key, bitmap);
        }
        return bitmap;
    }

    private static Bitmap decodeScaled(Context context, int resId, int size) {
        // Images live in drawable/ (mdpi), so the default decode upscales them by screen density.
        // Decode at original size, downsampled close to the target, then scale exactly.
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        bounds.inScaled = false;
        BitmapFactory.decodeResource(context.getResources(), resId, bounds);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inSampleSize = 1;
        int largestSide = Math.max(bounds.outWidth, bounds.outHeight);
        while (largestSide / (options.inSampleSize * 2) >= size) {
            options.inSampleSize *= 2;
        }

        Bitmap raw = BitmapFactory.decodeResource(context.getResources(), resId, options);
        Bitmap scaled = Bitmap.createScaledBitmap(raw, size, size, true);
        if (scaled != raw) {
            raw.recycle();
        }
        return scaled;
    }
}
