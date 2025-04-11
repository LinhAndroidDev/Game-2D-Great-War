package com.example.game2dgreatwar.gamepanel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.content.ContextCompat;

import com.example.game2dgreatwar.GameDisplay;
import com.example.game2dgreatwar.R;
import com.example.game2dgreatwar.gameobject.Player;

/**
 * HealthBar display the players health to the screen
 */
public class HealthBar {
    private final Player player;
    private final Paint borderPaint;
    private final Paint healthPaint;
    private final int width;
    private final int height;
    private final int margin; // pixel value

    public HealthBar(Context context, Player player) {
        this.player = player;
        this.width = 100;
        this.height = 20;
        this.margin = 2;

        this.borderPaint = new Paint();
        int borderColor = ContextCompat.getColor(context, R.color.healthBarBorder);
        borderPaint.setColor(borderColor);

        this.healthPaint = new Paint();
        int healthColor = ContextCompat.getColor(context, R.color.healthBarHealth);
        healthPaint.setColor(healthColor);
    }

    public void draw(Canvas canvas, GameDisplay gameDisplay) {
        float x = (float) player.getPositionX();
        float y = (float) player.getPositionY();
        float distanceToPlayer = 30;
        float healthPointPercentage = (float) player.getHealthPoint() / Player.MAX_HEALTH_POINTS;

        // Draw border
        float borderLeft, borderTop, borderRight, borderBottom;
        borderLeft = x - (float) width / 2;
        borderRight = x + (float) width / 2;
        borderBottom = y - distanceToPlayer;
        borderTop = borderBottom - height;
        canvas.drawRect(
                (float) gameDisplay.gameToDisplayCoordinatesX(borderLeft),
                (float) gameDisplay.gameToDisplayCoordinatesY(borderTop),
                (float) gameDisplay.gameToDisplayCoordinatesX(borderRight),
                (float) gameDisplay.gameToDisplayCoordinatesY(borderBottom),
                borderPaint);

        // Draw health
        float healthLeft, healthTop, healthRight, healthBottom, healthWidth, healthHeight;
        healthWidth = width - 2 * margin;
        healthHeight = height - 2 * margin;
        healthLeft = borderLeft + margin;
        healthRight = healthLeft + healthWidth * healthPointPercentage;
        healthBottom = borderBottom - margin;
        healthTop = healthBottom - healthHeight;
        canvas.drawRect(
                (float) gameDisplay.gameToDisplayCoordinatesX(healthLeft),
                (float) gameDisplay.gameToDisplayCoordinatesY(healthTop),
                (float) gameDisplay.gameToDisplayCoordinatesX(healthRight),
                (float) gameDisplay.gameToDisplayCoordinatesY(healthBottom),
                healthPaint);
    }
}
