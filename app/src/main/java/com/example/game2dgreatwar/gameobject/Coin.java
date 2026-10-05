package com.example.game2dgreatwar.gameobject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.R;
import com.example.game2dgreatwar.graphics.BitmapCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class Coin {
    private double positionX;
    private double positionY;
    private final int variant;
    private Bitmap bitmap;
    private static final int[] RES_IDS = { R.drawable.ic_coin, R.drawable.ic_coin_funny };
    private static final int BITMAP_SIZE = 50;
    private final double radius = BITMAP_SIZE; // Bán kính của Coin

    // Constructor có thêm resourceId để load ảnh Bitmap
    public Coin(Context context, double positionX, double positionY) {
        this(context, positionX, positionY, new Random().nextInt(RES_IDS.length));
    }

    private Coin(Context context, double positionX, double positionY, int variant) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.variant = Math.max(0, Math.min(RES_IDS.length - 1, variant));
        this.bitmap = BitmapCache.get(context, RES_IDS[this.variant], BITMAP_SIZE);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("x", positionX);
        json.put("y", positionY);
        // Store the image index, not the resource id: resource ids can change between builds
        json.put("variant", variant);
        return json;
    }

    public static Coin fromJson(Context context, JSONObject json) throws JSONException {
        return new Coin(context, json.getDouble("x"), json.getDouble("y"), json.getInt("variant"));
    }

    public static void preloadBitmaps(Context context) {
        for (int resId : RES_IDS) {
            BitmapCache.get(context, resId, BITMAP_SIZE);
        }
    }

    // Getter và Setter
    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(float positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(float positionY) {
        this.positionY = positionY;
    }

    // Hàm draw để vẽ ảnh lên canvas
    public void draw(Canvas canvas, GameDisplay gameDisplay) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, (float) gameDisplay.gameToDisplayCoordinatesX(positionX), (float) gameDisplay.gameToDisplayCoordinatesY(positionY), null);
        }
    }

    public boolean isColliding(Player player) {
        double distance = getDistanceBetweenObjects(player);
        double distanceToCollision = player.getRadius() + radius;
        return distance < distanceToCollision;
    }

    private double getDistanceBetweenObjects(Player player) {
        return Math.sqrt(
                Math.pow(player.getPositionX() - getPositionX(), 2) +
                        Math.pow(player.getPositionY() - getPositionY(), 2)
        );
    }
}
