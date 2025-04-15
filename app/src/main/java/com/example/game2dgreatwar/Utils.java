package com.example.game2dgreatwar;

import android.content.Context;
import android.media.MediaPlayer;

public class Utils {

    /**
     * getDistanceBetweenPoints returns the distance between 2d points p1 and p2
     * @param p1x
     * @param p1y
     * @param p2x
     * @param p2y
     * @return
     */
    public static double getDistanceBetweenPoints(double p1x, double p1y, double p2x, double p2y) {
        return Math.sqrt(Math.pow(p1x - p2x, 2) + Math.pow(p1y - p2y, 2));
    }

    public static void addSound(Context context, int soundId) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundId);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
    }

    // Check if circle and rectangle are colliding
    public static boolean isCollidingCircleRect(
            float circleX, float circleY, float radius,
            float rectX, float rectY, float rectWidth, float rectHeight) {

        // Tìm điểm gần nhất của hình chữ nhật với tâm hình tròn
        float closestX = clamp(circleX, rectX, rectX + rectWidth);
        float closestY = clamp(circleY, rectY, rectY + rectHeight);

        // Tính khoảng cách từ điểm gần nhất tới tâm hình tròn
        float distanceX = circleX - closestX;
        float distanceY = circleY - closestY;

        float distanceSquared = (distanceX * distanceX) + (distanceY * distanceY);

        return distanceSquared < (radius * radius);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
