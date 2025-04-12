package com.example.game2dgreatwar.gameobject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.R;

public class Health {
    private double positionX;
    private double positionY;
    private Bitmap bitmap;
    private final double radius = 50; // Bán kính của Health

    // Constructor có thêm resourceId để load ảnh Bitmap
    public Health(Context context) {
        this.positionX = 100 + Math.random() * (2000 - 100);
        this.positionY = 100 + Math.random() * (2000 - 100);
        this.bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_health);
        this.bitmap = Bitmap.createScaledBitmap(this.bitmap, (int) radius, (int) radius, true);
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